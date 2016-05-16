package app

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import models._
import reactivemongo.api.{ DB, MongoDriver }
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.bson.{ BSONDocument, BSONDocumentIdentity, BSONDocumentWriter }
import slick.driver.MySQLDriver.api._
import util.Logging
import com.typesafe.config.ConfigFactory
import java.nio.file.Files
import java.nio.file.Paths
import reactivemongo.api.MongoConnection
import storage.mongo._

object DbConverter {

  def main(args: Array[String]) = new DbConverter().process()

}

class DbConverter extends Logging {

  def process() = {

    logger.info("Starting DbConverter ...")

    logger.info("Connecting to DB ...")

    val configOverridePath = Paths get "instance.conf"

    val config = if (Files exists configOverridePath)
      ConfigFactory.parseFile(configOverridePath.toFile).withFallback(ConfigFactory.load())
    else
      ConfigFactory.load()

    implicit val viscachaDb = Database.forConfig("viscacha", config)

    val mongoDriver = new MongoDriver(Some(config))
    val mongoUri = MongoConnection.parseURI(config.getString("mongodb.uri")).get
    val mongoConnection = mongoDriver.connection(mongoUri)

    try {

      implicit val mongoDb = mongoConnection("fnb")

      try {

        val aggregateDataFuture = for {
          users <- fetchViscachaUsers
          groups <- fetchViscachaGroups
          categories <- fetchViscachaCategories
          forums <- fetchViscachaForums
          topics <- fetchViscachaTopics
          replies <- fetchViscachaReplies
          uploads <- fetchViscachaUploads
          permissions <- fetchViscachaForumPermissions
        } yield AggregateData(ViscachaForumData(users, groups, categories, forums, topics, replies, uploads, permissions))

        val insertFuture = for {
          aggregateData <- aggregateDataFuture
          insertedGroups <- insertData(aggregateData.groups, "groups")
          insertedUsers <- insertData(aggregateData.users, "users")
          insertedCategories <- insertData(aggregateData.categories, "categories")
          insertedForums <- insertData(aggregateData.forums, "forums")
          insertedThreads <- insertData(aggregateData.threads, "threads")
          insertedPosts <- insertData(aggregateData.posts, "posts")
          insertedPermissions <- insertData(aggregateData.permissions, "permissions")
        } yield insertedUsers + insertedCategories + insertedForums + insertedThreads + insertedPosts + insertedPermissions

        val inserted = Await.result(insertFuture, Duration.Inf)

        logger.info(s"Successfully inserted $inserted entities")

      } finally {
        mongoConnection.close()
        mongoConnection.actorSystem.terminate()
      }

    } finally viscachaDb.close()

    logger.info("All Done.")

  }

  def fetchViscachaUsers(implicit db: Database) = db.run(TableQuery[ViscachaUsers].result)

  def fetchViscachaGroups(implicit db: Database) = db.run(TableQuery[ViscachaGroups].result)

  def fetchViscachaCategories(implicit db: Database) = db.run(TableQuery[ViscachaCategories].sortBy { _.position }.result)

  def fetchViscachaForums(implicit db: Database) = db.run(TableQuery[ViscachaForums].sortBy { _.position }.result)

  def fetchViscachaTopics(implicit db: Database) = db.run(TableQuery[ViscachaTopics].sortBy { _.id }.result)

  def fetchViscachaReplies(implicit db: Database) = db.run(TableQuery[ViscachaReplies].sortBy { _.id }.result)

  def fetchViscachaUploads(implicit db: Database) = db.run(TableQuery[ViscachaUploads].sortBy { _.id }.result)

  def fetchViscachaForumPermissions(implicit db: Database) = db.run(TableQuery[ViscachaForumPermissions].sortBy { _.id }.result)

  def insertData[T](data: Seq[T], collectionName: String)(implicit db: DB, writer: BSONDocumentWriter[T]): Future[Int] = {
    val collection = db.collection[BSONCollection](collectionName)
    val entities = data.map(writer.write).toStream
    collection.remove(BSONDocument()).flatMap {
      _ => collection.bulkInsert(entities, ordered = true)
    } map {
      case MultiBulkWriteResult(ok, inserts, _, _, _, _, _, _, _) =>
        logger.info(s"Inserted $inserts entries into $collectionName")
        inserts
    }
  }

}