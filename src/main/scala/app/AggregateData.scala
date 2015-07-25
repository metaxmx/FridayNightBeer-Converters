package app

import models._
import models.ForumPermissions._
import util.Logging
import util.Converter._
import org.joda.time.DateTime
import util.Joda._

class AggregateData(viscachaData: ViscachaForumData) extends Logging {

  lazy val userMap = viscachaData.users.map { user => user.name -> user.id }.toMap

  def aggregate: FnbForumData = {

    logger.info("Aggregating data ...")

    val users = viscachaData.users map {
      user =>
        User(user.id, user.name.toLowerCase, user.pw, user.name, checkEmpty(user.fullname).map(unescapeViscacha),
          checkEmpty(user.pic).map { _.replace("uploads/pics/", "") }, None)
    }

    // TODO
    val groups = Seq[Group]()

    val guestGroupOpt = viscachaData.groups.find { _.guest }

    val categories = viscachaData.categories map {
      cat => ForumCategory(cat.id, unescapeViscacha(cat.name), cat.position, None)
    }

    val posts = viscachaData.replies map {
      reply =>
        Post(
          reply.id,
          reply.topic_id,
          convertContent(reply.comment),
          reply.name.toInt,
          new DateTime(reply.date * 1000),
          parseEdits(reply.edit))
    }

    val postsByThread = posts groupBy { _.thread } mapValues { _ sortBy { _.dateCreated } }
    val firstPostByThread = postsByThread mapValues { _.head }
    val lastChangeByThread = postsByThread mapValues { _.map { latestChange(_) }.sortBy { _.date }.reverse } mapValues { _.head }

    val forumIdsWithGuestAccess = viscachaData.forumPermissions.filter {
      p => p.group == 0 || guestGroupOpt.exists { _.id == p.group }
    }.filter { _.forumAccess }.map { _.forum }.toSet

    val forums = viscachaData.forums map {
      forum =>
        {
          val forumPermissions: Seq[AccessRule] = if (forumIdsWithGuestAccess.contains(forum.id))
            Seq()
          else
            Seq(AccessRule(Access.toString, None, None, None, None, false))
          Forum(forum.id, unescapeViscacha(forum.name), Some(forum.description).map(unescapeViscacha),
            forum.parent, forum.position, forum.readonly > 0, forumPermissions)
        }
    }

    val threads = viscachaData.topics map {
      topic =>
        {
          val firstPost = firstPostByThread(topic.id)
          val latestChange = lastChangeByThread(topic.id)
          val posts = postsByThread(topic.id)
          Thread(topic.id, unescapeViscacha(topic.topic), topic.board,
            ThreadPostData(firstPost.userCreated, firstPost.dateCreated),
            latestChange, posts.size, (topic.sticky > 0), None)
        }
    }

    FnbForumData(users, groups, categories, forums, threads, posts)
  }

  def parseEdits(edits: String): Option[Seq[PostEdit]] = checkEmpty(edits) flatMap {
    edit =>
      {
        val editObjs = wrapRefArray(edit.split('\n')) map {
          _.split('\t')
        } filter {
          parts => userMap contains parts(0)
        } map {
          parts => PostEdit(userMap.get(parts(0)).get, new DateTime(parts(1).toLong * 1000), checkEmpty(parts(2)), parts(3))
        }
        checkEmpty(editObjs)
      }
  }

  def initialPost(post: Post): ThreadPostData = ThreadPostData(post.userCreated, post.dateCreated)

  def latestChange(post: Post): ThreadPostData = {
    val p = initialPost(post)
    val e = post.edits.map { _ map { edit => ThreadPostData(edit.user, edit.date) } } getOrElse (Seq())
    (p +: e).maxBy { _.date }
  }

}

object AggregateData {

  def apply(viscachaData: ViscachaForumData): FnbForumData = new AggregateData(viscachaData).aggregate

}
