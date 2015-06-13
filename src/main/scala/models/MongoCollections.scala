package models

import reactivemongo.bson._
import org.joda.time.DateTime
import models.JodaFormat.BSONDateTimeHandler

/*
 * --- User ---
 */

case class FnbUser(
  _id: Int,
  username: String,
  password: String,
  displayName: String,
  fullName: Option[String])

object FnbUser {

  implicit object FnbUserWriter extends BSONDocumentWriter[FnbUser] {
    def write(user: FnbUser): BSONDocument = BSONDocument(
      "_id" -> user._id,
      "username" -> user.username,
      "password" -> user.password,
      "displayName" -> user.displayName,
      "fullName" -> user.fullName)
  }

  implicit object FnbUserReader extends BSONDocumentReader[FnbUser] {
    def read(doc: BSONDocument): FnbUser = {
      FnbUser(
        doc.getAs[Int]("_id").get,
        doc.getAs[String]("username").get,
        doc.getAs[String]("password").get,
        doc.getAs[String]("displayName").get,
        doc.getAs[String]("fullName"))
    }
  }

}

/*
 * --- Category ---
 */

case class FnbCategory(
  _id: Int,
  name: String,
  position: Int)

object FnbCategory {

  implicit object FnbCategoryWriter extends BSONDocumentWriter[FnbCategory] {
    def write(cat: FnbCategory): BSONDocument = BSONDocument(
      "_id" -> cat._id,
      "name" -> cat.name,
      "position" -> cat.position)
  }

  implicit object FnbCategoryReader extends BSONDocumentReader[FnbCategory] {
    def read(doc: BSONDocument): FnbCategory = {
      FnbCategory(
        doc.getAs[Int]("_id").get,
        doc.getAs[String]("name").get,
        doc.getAs[Int]("position").get)
    }
  }

}

/*
 * --- Forum ---
 */

case class FnbForum(
  _id: Int,
  name: String,
  description: Option[String],
  category: Int,
  position: Int,
  readonly: Boolean)

object FnbForum {

  implicit object FnbForumWriter extends BSONDocumentWriter[FnbForum] {
    def write(forum: FnbForum): BSONDocument = BSONDocument(
      "_id" -> forum._id,
      "name" -> forum.name,
      "description" -> forum.description,
      "category" -> forum.category,
      "position" -> forum.position,
      "readonly" -> forum.readonly)
  }

  implicit object FnbForumReader extends BSONDocumentReader[FnbForum] {
    def read(doc: BSONDocument): FnbForum = {
      FnbForum(
        doc.getAs[Int]("_id").get,
        doc.getAs[String]("name").get,
        doc.getAs[String]("description"),
        doc.getAs[Int]("category").get,
        doc.getAs[Int]("position").get,
        doc.getAs[Boolean]("readonly").get)
    }
  }

}

/*
 * --- Thread ---
 */

case class FnbThread(
  _id: Int,
  forum: Int,
  topic: String,
  userCreated: Int,
  dateCreated: DateTime,
  sticky: Boolean)

object FnbThread {

  implicit object FnbThreadWriter extends BSONDocumentWriter[FnbThread] {
    def write(thread: FnbThread): BSONDocument = BSONDocument(
      "_id" -> thread._id,
      "forum" -> thread.forum,
      "topic" -> thread.topic,
      "created" -> BSONDocument(
        "user" -> thread.userCreated,
        "date" -> thread.dateCreated),
      "sticky" -> thread.sticky)
  }

  implicit object FnbThreadReader extends BSONDocumentReader[FnbThread] {
    def read(doc: BSONDocument): FnbThread = {
      FnbThread(
        doc.getAs[Int]("_id").get,
        doc.getAs[Int]("forum").get,
        doc.getAs[String]("topic").get,
        doc.getAs[BSONDocument]("created").get.getAs[Int]("user").get,
        doc.getAs[BSONDocument]("created").get.getAs[DateTime]("date").get,
        doc.getAs[Boolean]("sticky").get)
    }
  }

}
