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
import util.Converter._
import org.joda.time.DateTime

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
        val fnbForumsCollection = mongoDb.collection[BSONCollection]("forums")
        val fnbTopicsCollection = mongoDb.collection[BSONCollection]("topics")

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

        logger.info("Converting Categories and Forums ...")

        val fetchForumsFuture = fetchViscachaForums
        val insertForumsFuture = insertFnbForums(fnbForumsCollection, fetchForumsFuture)
        insertForumsFuture.onComplete {
          case Success(count) => logger.info(s"Successfull inserted $count forums")
          case Failure(exc)   => logger.error("Error inserting forums", exc)
        }
        Await.result(insertForumsFuture, Duration.Inf)

        // Convert Threads

        logger.info("Converting Threads/Topics ...")

        val fetchTopicsFuture = fetchViscachaTopics
        val insertThreadsFuture = insertFnbThreads(fnbTopicsCollection, fetchTopicsFuture)
        insertThreadsFuture.onComplete {
          case Success(count) => logger.info(s"Successfull inserted $count threads")
          case Failure(exc)   => logger.error("Error inserting threads", exc)
        }
        Await.result(insertThreadsFuture, Duration.Inf)

      } finally mongoConnection.close

    } finally viscachaDb.close

    logger.info("All Done.")

  }

  def fetchViscachaUsers(implicit viscachaDb: Database): Future[Seq[ViscachaUser]] =
    viscachaDb.run(TableQuery[ViscachaUsers].result)

  def insertFnbUsers(fnbUserCollection: BSONCollection, usersFuture: Future[Seq[ViscachaUser]]) = {
    usersFuture map {
      _ map { user => FnbUser(user.id, user.name.toLowerCase, user.pw, user.name, checkEmpty(user.fullname).map(unescapeViscacha)) }
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
      _ map { cat => FnbCategory(cat.id, unescapeViscacha(cat.name), cat.position) }
    } flatMap {
      vCats =>
        fnbCategoriesCollection.remove(BSONDocument()).flatMap {
          lastError =>
            vCats.foreach { cat => logger.debug(s"Insert: $cat") }
            fnbCategoriesCollection.bulkInsert(Enumerator.enumerate(vCats))
        }
    }
  }

  def fetchViscachaForums(implicit viscachaDb: Database): Future[Seq[ViscachaForum]] =
    viscachaDb.run(TableQuery[ViscachaForums].sortBy { _.position }.result)

  def insertFnbForums(fnbForumsCollection: BSONCollection, forumsFuture: Future[Seq[ViscachaForum]]) = {
    forumsFuture map {
      _ map { forum =>
        FnbForum(forum.id, unescapeViscacha(forum.name), Some(forum.description).map(unescapeViscacha),
          forum.parent, forum.position, forum.readonly > 0)
      }
    } flatMap {
      vForums =>
        fnbForumsCollection.remove(BSONDocument()).flatMap {
          lastError =>
            vForums.foreach { forum => logger.debug(s"Insert: $forum") }
            fnbForumsCollection.bulkInsert(Enumerator.enumerate(vForums))
        }
    }
  }

  def fetchViscachaTopics(implicit viscachaDb: Database): Future[Seq[ViscachaTopic]] =
    viscachaDb.run(TableQuery[ViscachaTopics].sortBy { _.id }.result)

  def insertFnbThreads(fnbThreadsCollection: BSONCollection, topicsFuture: Future[Seq[ViscachaTopic]]) = {
    topicsFuture map {
      _ map { topic => FnbThread(topic.id, topic.board, unescapeViscacha(topic.topic), topic.name, new DateTime(topic.date * 1000), topic.sticky > 0) }
    } flatMap {
      threads =>
        fnbThreadsCollection.remove(BSONDocument()).flatMap {
          lastError =>
            threads.foreach { thread => logger.debug(s"Insert: $thread") }
            fnbThreadsCollection.bulkInsert(Enumerator.enumerate(threads))
        }
    }
  }

}