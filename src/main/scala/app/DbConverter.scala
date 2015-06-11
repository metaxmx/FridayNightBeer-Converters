package app

import scala.concurrent.{ Future, Await }
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
        val fnbUserCollection = mongoDb.collection[BSONCollection]("users")
        val fnbCategoriesCollection = mongoDb.collection[BSONCollection]("categories")

        // Convert Users

        logger.info("Converting Users ...")

        val fetchUsersFuture = fetchViscachaUsers
        val insertUsersFuture = insertFnbUsers(fnbUserCollection, fetchUsersFuture)
        insertUsersFuture.onComplete {
          case Success(count) => logger.info(s"Successfull inserted $count users")
          case Failure(exc)   => logger.error("Error inserting users", exc)
        }
        Await.result(insertUsersFuture, Duration.Inf)

        // Convert Categories and Forums

        logger.info("Converting Categories and Forums ...")

        val fetchCategoriesFuture = fetchViscachaCategories
        val insertCategoriesFuture = insertFnbCategories(fnbCategoriesCollection, fetchCategoriesFuture)
        insertCategoriesFuture.onComplete {
          case Success(count) => logger.info(s"Successfull inserted $count categories")
          case Failure(exc)   => logger.error("Error inserting categories", exc)
        }
        Await.result(insertCategoriesFuture, Duration.Inf)

      } finally mongoConnection.close

    } finally viscachaDb.close

    logger.info("All Done.")

  }

  def fetchViscachaUsers(implicit viscachaDb: Database): Future[Seq[ViscachaUser]] =
    viscachaDb.run(TableQuery[ViscachaUsers].result)

  def insertFnbUsers(fnbUserCollection: BSONCollection, usersFuture: Future[Seq[ViscachaUser]]) = {
    usersFuture map {
      _ map { user => FnbUser(user.id, user.name.toLowerCase, user.pw, user.name, user.fullname) }
    } flatMap {
      vUsers =>
        fnbUserCollection.remove(BSONDocument()).flatMap {
          lastError =>
            vUsers.foreach { user => logger.debug(s"Insert: $user") }
            fnbUserCollection.bulkInsert(Enumerator.enumerate(vUsers))
        }
    }
  }

  def fetchViscachaCategories(implicit viscachaDb: Database): Future[Seq[ViscachaCategory]] =
    viscachaDb.run(TableQuery[ViscachaCategories].sortBy { _.position }.result)

  def insertFnbCategories(fnbCategoriesCollection: BSONCollection, categoriesFuture: Future[Seq[ViscachaCategory]]) = {
    categoriesFuture map {
      _ map { cat => FnbCategory(cat.id, cat.name, cat.position) }
    } flatMap {
      vCats =>
        fnbCategoriesCollection.remove(BSONDocument()).flatMap {
          lastError =>
            vCats.foreach { user => logger.debug(s"Insert: $user") }
            fnbCategoriesCollection.bulkInsert(Enumerator.enumerate(vCats))
        }
    }
  }

}