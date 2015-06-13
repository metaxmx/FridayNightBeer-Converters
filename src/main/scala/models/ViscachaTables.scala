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

