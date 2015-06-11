package models

import reactivemongo.bson._

case class FnbUser(
  _id: Int,
  username: String,
  password: String,
  displayName: String,
  fullName: String)

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
        doc.getAs[String]("fullName").get)
    }
  }

}

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