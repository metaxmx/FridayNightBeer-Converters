package app

import java.io.IOException
import java.nio.file.{Files, Path, Paths, SimpleFileVisitor}
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.attribute.BasicFileAttributes

import org.joda.time.DateTime
import models.{AccessRule, FnbForumData, Forum, ForumCategory}
import models.{Group, Post, PostEdit, PostUpload, Thread, ThreadPostData, User, ViscachaForumData, ViscachaUpload}
import models.ForumPermissions.Access
import reactivemongo.bson.BSONObjectID
import util.Converter.{checkEmpty, convertContent, unescapeViscacha}
import util.Joda.dateTimeOrdering
import util.Logging
import storage.mongo._

import scala.collection.mutable

class AggregateData(viscachaData: ViscachaForumData) extends Logging {

  lazy val userMap = viscachaData.users.map { user => user.name -> convertUserId(user.id) }.toMap

  val pathToFnb = Paths get "../FridayNightBeer"

  val pathToViscachaUploads = pathToFnb resolve "appdata/_viscacha_uploads"

  val pathToFnbUploads = pathToFnb resolve "appdata/uploads"

  val pathToFnbAvatars = pathToFnb resolve "appdata/avatars"

  val entityIdMap = Seq("user", "group", "category", "forum", "thread", "post").map(entity => (entity, mutable.Map.empty[Long, String])).toMap

  def convertId(entityType: String, id: Long): String = entityIdMap(entityType).getOrElseUpdate(id, BSONObjectID.generate.stringify)

  def convertUserId(id: Long) = convertId("user", id)

  def convertGroupId(id: Long) = convertId("group", id)

  def convertCatId(id: Long) = convertId("category", id)

  def convertForumId(id: Long) = convertId("forum", id)

  def convertThreadId(id: Long) = convertId("thread", id)

  def convertPostId(id: Long) = convertId("post", id)

  def aggregate: FnbForumData = {

    logger.info("Aggregating data ...")

    val groupIdsByUserId = viscachaData.users.map {
      user => convertUserId(user.id) -> user.groups.split(',').toSet.filterNot { _.isEmpty }.map { gr => convertGroupId(gr.toInt) }
    }.toMap

    val usedGroupIds = groupIdsByUserId.values.flatten.toSet

    val (vCoreGroups, vCustomGroups) = viscachaData.groups.partition { _.core }
    val vGuestGroupOpt = vCoreGroups.find { _.guest }
    val vAdminGroupOpt = vCoreGroups.find { _.admin }
    val vSupermodGroupOpt = vCoreGroups.find { g => g.gmod && !g.admin }

    val adminGroup = Group("admin", "Admins")
    val supermodGroup = Group("supermod", "Super Mod")
    val predefinedGroups = Seq(adminGroup, supermodGroup)

    val customGroupsMap = vCustomGroups.filter { g => usedGroupIds contains convertGroupId(g.id) }.map {
      vGroup => convertGroupId(vGroup.id) -> Group(vGroup.name, vGroup.title)
    }.toMap
    val customGroups = customGroupsMap.values

    val groupsMap = customGroupsMap ++
      vAdminGroupOpt.map { g => Seq(convertGroupId(g.id) -> adminGroup) }.getOrElse(Seq()) ++
      vSupermodGroupOpt.map { g => Seq(convertGroupId(g.id) -> supermodGroup) }.getOrElse(Seq())

    val groups = predefinedGroups ++ customGroups

    val users = viscachaData.users map {
      user =>
        val groupsForUser = groupIdsByUserId.get(convertUserId(user.id)).map {
          _.filter { groupsMap.contains }.map { groupsMap.apply }.map { _._id }.toSeq
          }.flatMap { groupList => if (groupList.isEmpty) None else Some(groupList) }
        User(convertUserId(user.id), user.name.toLowerCase, user.pw, user.name, checkEmpty(user.fullname).map(unescapeViscacha),
          checkEmpty(user.pic).map { _.replace("uploads/pics/", "") }.filter { pic => Files exists (pathToFnbAvatars resolve pic) },
          groupsForUser)
    }

    val categories = viscachaData.categories map {
      cat => ForumCategory(convertCatId(cat.id), unescapeViscacha(cat.name), cat.position, None)
    }

    if (Files isDirectory pathToFnbUploads) {
      delTree(pathToFnbUploads)
    }

    val uploadsByPost = viscachaData.uploads.map {
      ul => ul.post_id -> precessUpload(ul)
    }.filter {
      case (post_id, uploadOpt) => uploadOpt.isDefined
    }.map {
      case (post_id, uploadOpt) => convertPostId(post_id) -> uploadOpt.get
    }.groupBy { _._1 }.mapValues { _ map (_._2) }

    val posts = viscachaData.replies map {
      reply =>
        Post(
          convertPostId(reply.id),
          convertThreadId(reply.topic_id),
          convertContent(reply.comment),
          convertUserId(reply.name.toInt),
          new DateTime(reply.date * 1000),
          parseEdits(reply.edit),
          uploadsByPost.getOrElse(convertPostId(reply.id), Seq()))
    }

    val postsByThread = posts groupBy { _.thread } mapValues { _ sortBy { _.dateCreated } }
    val firstPostByThread = postsByThread mapValues { _.head }
    val lastChangeByThread = postsByThread mapValues { _.map { latestChange(_) }.sortBy { _.date }.reverse } mapValues { _.head }

    val forumIdsWithGuestAccess = viscachaData.forumPermissions.filter {
      p => p.group == 0 || vGuestGroupOpt.exists { _.id == p.group }
    }.filter { _.forumAccess }.map { p => convertForumId(p.forum) }.toSet

    val forums = viscachaData.forums map {
      forum =>
        val forumPermissions: Option[Seq[AccessRule]] = if (forumIdsWithGuestAccess.contains(convertForumId(forum.id)))
          None
        else
          Some(Seq(AccessRule(Access.toString, None, None, None, None, allowGuest = false)))
        Forum(convertForumId(forum.id), unescapeViscacha(forum.name), Some(forum.description).map(unescapeViscacha),
          convertCatId(forum.parent), forum.position, forum.readonly > 0, forumPermissions)
    }

    val threads = viscachaData.topics map {
      topic =>
        val firstPost = firstPostByThread(convertThreadId(topic.id))
        val latestChange = lastChangeByThread(convertThreadId(topic.id))
        val posts = postsByThread(convertThreadId(topic.id))
        Thread(convertThreadId(topic.id), unescapeViscacha(topic.topic), convertForumId(topic.board),
          ThreadPostData(firstPost.userCreated, firstPost.dateCreated),
          latestChange, posts.size, topic.sticky > 0, None)
    }

    FnbForumData(users, groups, categories, forums, threads, posts)
  }

  def parseEdits(edits: String): Option[Seq[PostEdit]] = checkEmpty(edits) flatMap {
    edit =>
      val editObjs = wrapRefArray(edit.split('\n')) map {
        _.split('\t')
      } filter {
        parts => userMap contains parts(0)
      } map {
        parts => PostEdit(userMap.get(parts(0)).get, new DateTime(parts(1).toLong * 1000), checkEmpty(parts(2)), parts(3))
      }
      checkEmpty(editObjs)
  }

  def initialPost(post: Post): ThreadPostData = ThreadPostData(post.userCreated, post.dateCreated)

  def latestChange(post: Post): ThreadPostData = {
    val p = initialPost(post)
    val e = post.edits.map { _ map { edit => ThreadPostData(edit.user, edit.date) } } getOrElse Seq()
    (p +: e).maxBy { _.date }
  }

  def precessUpload(upload: ViscachaUpload): Option[PostUpload] = {
    val path = pathToViscachaUploads resolve upload.source
    if (Files exists path) {
      val size = Files size path
      val lastModified = Files getLastModifiedTime path
      val imageData = None
      val targetPath = pathToFnbUploads resolve upload.post_id.toString resolve upload.source
      Files.createDirectories(targetPath.getParent)
      Files.copy(path, targetPath, COPY_ATTRIBUTES)
      Some(PostUpload(upload.user_id, new DateTime(lastModified.toMillis), upload.file, upload.source, size, imageData, upload.hits))
    } else {
      logger.error(s"Upload File not found: ${upload.source}")
      None
    }
  }

  def delTree(path: Path) = Files.walkFileTree(path, new SimpleFileVisitor[Path] {
    override def visitFile(file: Path, attrs: BasicFileAttributes) = { Files.delete(file); CONTINUE }
    override def postVisitDirectory(dir: Path, exc: IOException) = { Files.delete(dir); CONTINUE }
  });

}

object AggregateData {

  def apply(viscachaData: ViscachaForumData): FnbForumData = new AggregateData(viscachaData).aggregate

}
