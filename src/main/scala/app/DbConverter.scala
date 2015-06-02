package app

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.slick.driver.MySQLDriver.api._
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import play.api.libs.iteratee.Enumerator
import scala.util._

import models._
import util._

object DbConverter extends Logging {

  def main(args: Array[String]) = {

    logger.info("Starting DbConverter ...")

    logger.info("Connecting to DB ...")

    implicit val viscachaDb = Database.forConfig("viscacha")
    try {

      val mongoDriver = new MongoDriver
      val mongoConnection = mongoDriver.connection(List("localhost"))
      try {
        val mongoDb = mongoConnection.db("fnb")
        val fnbUsercollection = mongoDb.collection[BSONCollection]("users")

        // Convert Users

        logger.info("Converting Users ...")

        val fetchUsersFuture = fetchViscachaUsers
        val insertUsersFuture = insertFnbUsers(fnbUsercollection, fetchUsersFuture)
        insertUsersFuture.onComplete {
          case Success(count) => logger.info(s"Successfull inserted $count users")
          case Failure(exc)   => logger.error("Error inserting users", exc)
        }
        Await.result(insertUsersFuture, Duration.Inf)

      } finally mongoConnection.close

    } finally viscachaDb.close

    logger.info("All Done.")

  }

  def fetchViscachaUsers(implicit viscachaDb: Database): Future[Seq[ViscachaUser]] = viscachaDb.run(TableQuery[ViscachaUsers].result)

  def insertFnbUsers(fnbUsercollection: BSONCollection, usersFuture: Future[Seq[ViscachaUser]]) = {
    usersFuture map {
      _ map { user => FnbUser(BSONObjectID.generate, user.name, user.pw, user.name, user.fullname) }
    } flatMap {
      vUsers =>
        fnbUsercollection.remove(BSONDocument()).flatMap {
          lastError =>
            vUsers.foreach { user => logger.debug(s"Insert: $user") }
            fnbUsercollection.bulkInsert(Enumerator.enumerate(vUsers))
        }
    }
  }

}