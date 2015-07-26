package models

import slick.driver.MySQLDriver.api._
import slick.lifted.{ ProvenShape, ForeignKeyQuery }

/*
 * --- User ---
 */

case class ViscachaUser(
  id: Int,
  name: String,
  pw: String,
  mail: String,
  regdate: Long,
  fullname: String,
  signature: String,
  location: String,
  gender: String,
  birthday: Option[String],
  pic: String,
  lastvisit: Long)

class ViscachaUsers(tag: Tag)
  extends Table[ViscachaUser](tag, "v_user") {

  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def pw = column[String]("pw")
  def mail = column[String]("mail")
  def regdate = column[Long]("regdate")
  def fullname = column[String]("fullname")
  def signature = column[String]("signature")
  def location = column[String]("location")
  def gender = column[String]("gender")
  def birthday = column[Option[String]]("birthday")
  def pic = column[String]("pic")
  def lastvisit = column[Long]("lastvisit")

  def * = (id, name, pw, mail, regdate, fullname, signature, location, gender, birthday, pic, lastvisit) <> (ViscachaUser.tupled, ViscachaUser.unapply)
}

/*
 * --- Group ---
 */

case class ViscachaGroup(
  id: Int,
  admin: Boolean,
  guest: Boolean,
  members: Boolean,
  profile: Boolean,
  pm: Boolean,
  wwo: Boolean,
  search: Boolean,
  team: Boolean,
  usepic: Boolean,
  useabout: Boolean,
  usesignature: Boolean,
  downloadfiles: Boolean,
  forum: Boolean,
  posttopics: Boolean,
  postreplies: Boolean,
  addvotes: Boolean,
  attachments: Boolean,
  edit: Boolean,
  voting: Boolean,
  title: String)

class ViscachaGroups(tag: Tag)
  extends Table[ViscachaGroup](tag, "v_groups") {

  def id = column[Int]("id", O.PrimaryKey)
  def admin = column[Boolean]("admin")
  def guest = column[Boolean]("guest")
  def members = column[Boolean]("members")
  def profile = column[Boolean]("profile")
  def pm = column[Boolean]("pm")
  def wwo = column[Boolean]("wwo")
  def search = column[Boolean]("search")
  def team = column[Boolean]("team")
  def usepic = column[Boolean]("usepic")
  def useabout = column[Boolean]("useabout")
  def usesignature = column[Boolean]("usesignature")
  def downloadfiles = column[Boolean]("downloadfiles")
  def forum = column[Boolean]("forum")
  def posttopics = column[Boolean]("posttopics")
  def postreplies = column[Boolean]("postreplies")
  def addvotes = column[Boolean]("addvotes")
  def attachments = column[Boolean]("attachments")
  def edit = column[Boolean]("edit")
  def voting = column[Boolean]("voting")
  def title = column[String]("title")

  def * = (id, admin, guest, members, profile, pm, wwo, search, team, usepic, useabout, usesignature, downloadfiles, forum, posttopics, postreplies, addvotes, attachments, edit, voting, title) <> (ViscachaGroup.tupled, ViscachaGroup.unapply)
}

/*
 * --- Category ---
 */

case class ViscachaCategory(
  id: Int,
  name: String,
  position: Int)

class ViscachaCategories(tag: Tag)
  extends Table[ViscachaCategory](tag, "v_categories") {

  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def position = column[Int]("position")

  def * = (id, name, position) <> (ViscachaCategory.tupled, ViscachaCategory.unapply)
}

/*
 * --- Forum ---
 */

case class ViscachaForum(
  id: Int,
  name: String,
  description: String,
  parent: Int,
  position: Int,
  readonly: Int)

class ViscachaForums(tag: Tag)
  extends Table[ViscachaForum](tag, "v_forums") {

  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def parent = column[Int]("parent")
  def position = column[Int]("position")
  def readonly = column[Int]("readonly")

  def * = (id, name, description, parent, position, readonly) <> (ViscachaForum.tupled, ViscachaForum.unapply)
}

/*
 * Forum Group Restrictions
 */

case class ViscachaForumPermission(
  id: Int,
  group: Int,
  forum: Int,
  forumAccess: Boolean,
  downloadFiles: Boolean,
  createTopics: Boolean,
  createReplies: Boolean,
  createPolls: Boolean,
  attachments: Boolean,
  editPosts: Boolean,
  vote: Boolean)

class ViscachaForumPermissions(tag: Tag)
  extends Table[ViscachaForumPermission](tag, "v_fgroups") {

  def id = column[Int]("fid", O.PrimaryKey)
  def group = column[Int]("gid")
  def forum = column[Int]("bid")
  def forumAccess = column[Boolean]("f_forum")
  def downloadFiles = column[Boolean]("f_downloadfiles")
  def createTopics = column[Boolean]("f_posttopics")
  def createReplies = column[Boolean]("f_postreplies")
  def createPolls = column[Boolean]("f_addvotes")
  def attachments = column[Boolean]("f_attachments")
  def editPosts = column[Boolean]("f_edit")
  def vote = column[Boolean]("f_voting")

  def * = (id, group, forum, forumAccess, downloadFiles, createTopics, createReplies, createPolls, attachments, editPosts, vote) <> (ViscachaForumPermission.tupled, ViscachaForumPermission.unapply)
}

/*
 * --- Topic ---
 */

case class ViscachaTopic(
  id: Int,
  board: Int,
  topic: String,
  name: Int,
  date: Long,
  status: Int,
  sticky: Int,
  question: String)

class ViscachaTopics(tag: Tag)
  extends Table[ViscachaTopic](tag, "v_topics") {

  def id = column[Int]("id", O.PrimaryKey)
  def board = column[Int]("board")
  def topic = column[String]("topic")
  def name = column[Int]("name")
  def date = column[Long]("date")
  def status = column[Int]("status")
  def sticky = column[Int]("sticky")
  def question = column[String]("vquestion")

  def * = (id, board, topic, name, date, status, sticky, question) <> (ViscachaTopic.tupled, ViscachaTopic.unapply)
}

/*
 * --- Reply ---
 */

case class ViscachaReply(
  id: Int,
  topic_id: Int,
  name: String,
  comment: String,
  ip: String,
  date: Long,
  edit: String)

class ViscachaReplies(tag: Tag)
  extends Table[ViscachaReply](tag, "v_replies") {

  def id = column[Int]("id", O.PrimaryKey)
  def topic_id = column[Int]("topic_id")
  def name = column[String]("name")
  def comment = column[String]("comment")
  def ip = column[String]("ip")
  def date = column[Long]("date")
  def edit = column[String]("edit")

  def * = (id, topic_id, name, comment, ip, date, edit) <> (ViscachaReply.tupled, ViscachaReply.unapply)
}

/*
 * --- Upload ---
 */

case class ViscachaUpload(
  id: Int,
  post_id: Int,
  topic_id: Int,
  user_id: Int,
  file: String,
  source: String,
  hits: Int)

class ViscachaUploads(tag: Tag)
  extends Table[ViscachaUpload](tag, "v_uploads") {

  def id = column[Int]("id", O.PrimaryKey)
  def post_id = column[Int]("tid")
  def topic_id = column[Int]("topic_id")
  def user_id = column[Int]("mid")
  def file = column[String]("file")
  def source = column[String]("source")
  def hits = column[Int]("hits")

  def * = (id, post_id, topic_id, user_id, file, source, hits) <> (ViscachaUpload.tupled, ViscachaUpload.unapply)
}
