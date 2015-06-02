package models

import reactivemongo.bson._

case class FnbUser(
  _id: BSONObjectID,
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
        doc.getAs[BSONObjectID]("_id").get,
        doc.getAs[String]("username").get,
        doc.getAs[String]("password").get,
        doc.getAs[String]("displayName").get,
        doc.getAs[String]("fullName").get)
    }
  }

}