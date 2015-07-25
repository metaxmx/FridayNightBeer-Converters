package app

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._
import reactivemongo.api._
import play.api.libs.iteratee.Enumerator
import scala.util._
import models._
import util._
import util.Converter._
import org.joda.time.DateTime
import scala.collection.mutable.WrappedArray
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._
import reactivemongo.bson.BSONWriter
import reactivemongo.api.commands.MultiBulkWriteResult
import scala.concurrent.Promise

object DbConverter {

  def main(args: Array[String]) = new DbConverter().process

}

class DbConverter extends Logging {

  def process = {

    logger.info("Starting DbConverter ...")

    logger.info("Connecting to DB ...")

    implicit val viscachaDb = Database.forConfig("viscacha")

    val mongoDriver = new MongoDriver
    val mongoConnection = mongoDriver.connection(List("localhost"))

    try {

      implicit val mongoDb = mongoConnection.db("fnb")

      try {

        val aggregateDataFuture = for {
          users <- fetchViscachaUsers
          groups <- fetchViscachaGroups
          categories <- fetchViscachaCategories
          forums <- fetchViscachaForums
          topics <- fetchViscachaTopics
          replies <- fetchViscachaReplies
          permissions <- fetchViscachaForumPermissions
        } yield AggregateData(ViscachaForumData(users, groups, categories, forums, topics, replies, permissions))

        val insertFuture = for {
          aggregateData <- aggregateDataFuture
          insertedUsers <- insertData(aggregateData.users)
          insertedCategories <- insertData(aggregateData.categories)
          insertedForums <- insertData(aggregateData.forums)
          insertedThreads <- insertData(aggregateData.threads)
          insertedPosts <- insertData(aggregateData.posts)
        } yield (insertedUsers + insertedCategories + insertedForums + insertedThreads + insertedPosts)

        val inserted = Await.result(insertFuture, Duration.Inf)

        logger.info(s"Successfully inserted $inserted entities")

      } finally {
        mongoConnection.close()
        mongoConnection.actorSystem.shutdown()
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

  def fetchViscachaForumPermissions(implicit db: Database) = db.run(TableQuery[ViscachaForumPermissions].sortBy { _.id }.result)

  def insertData[T](data: Seq[T])(implicit db: DB, writer: BSONDocumentWriter[T], baseModel: BaseModel[T]): Future[Int] = {
    val collection = db.collection[BSONCollection](baseModel.collectionName)
    val entities = data.map(writer.write(_)).toStream
    collection.remove(BSONDocument()).flatMap {
      _ => collection.bulkInsert(entities, true)
    } map {
      case MultiBulkWriteResult(ok, inserts, _, _, _, _, _, _, _) =>
        logger.info(s"Inserted $inserts entries into ${baseModel.collectionName}")
        inserts
    }
  }

}