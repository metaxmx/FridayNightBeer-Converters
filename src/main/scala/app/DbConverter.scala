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
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.Json
import play.api.libs.json.Writes

object DbConverter {

  def main(args: Array[String]) = new DbConverter().process

}

class DbConverter extends Logging {

  def process = {

    logger info "Starting DbConverter ..."

    logger info "Connecting to DB ..."

    implicit val viscachaDb = Database forConfig "viscacha"

    val mongoDriver = new MongoDriver
    val mongoConnection = mongoDriver.connection(List("localhost"))

    try {

      implicit val mongoDb = mongoConnection.db("fnb")

      // Execute

      try {

        val process = Future.successful(ViscachaForumData()) flatMap {
          data =>
            fetchViscachaUsers map {
              users =>
                logger info s"Fetched ${users.size} Users"
                data withUsers users
            }
        } flatMap {
          data =>
            fetchViscachaCategories map {
              categories =>
                logger info s"Fetched ${categories.size} Categories"
                data withCategories categories
            }
        } flatMap {
          data =>
            fetchViscachaForums map {
              forums =>
                logger info s"Fetched ${forums.size} Forums"
                data withForums forums
            }
        } flatMap {
          data =>
            fetchViscachaTopics map {
              topics =>
                logger info s"Fetched ${topics.size} Topics"
                data withTopics topics
            }
        } flatMap {
          data =>
            fetchViscachaReplies map {
              replies =>
                logger info s"Fetched ${replies.size} Replies"
                data withReplies replies
            }
        } map {
          new AggregateData(_).aggregate
        } flatMap {
          insertData(User.collectionName) { _.users }
        } flatMap {
          insertData(ForumCategory.collectionName) { _.categories }
        } flatMap {
          insertData(Forum.collectionName) { _.forums }
        }

        Await.result(process, Duration.Inf)

        // Stop Execute

      } finally {
        val closeFuture = mongoConnection.close
        mongoConnection.actorSystem.shutdown()
      }

    } finally viscachaDb.close

    logger.info("All Done.")

  }

  def fetchViscachaUsers(implicit db: Database) = db.run(TableQuery[ViscachaUsers].result)

  def fetchViscachaCategories(implicit db: Database) = db.run(TableQuery[ViscachaCategories].sortBy { _.position }.result)

  def fetchViscachaForums(implicit db: Database) = db.run(TableQuery[ViscachaForums].sortBy { _.position }.result)

  def fetchViscachaTopics(implicit db: Database) = db.run(TableQuery[ViscachaTopics].sortBy { _.id }.result)

  def fetchViscachaReplies(implicit db: Database) = db.run(TableQuery[ViscachaReplies].sortBy { _.id }.result)

  def insertData[T](collectionName: String)(resolve: FnbForumData => Seq[T])(implicit db: DB, writes: Writes[T]): FnbForumData => Future[FnbForumData] =
    data => {
      val collection = db.collection[JSONCollection](collectionName)
      val entities = resolve(data)
      collection.remove(Json.obj()).flatMap {
        lastError =>
          entities.foreach { entity => logger.debug(s"Insert: $entity") }
          collection.bulkInsert(Enumerator.enumerate(entities))
      } map {
        inserts =>
          logger info s"Inserted $inserts entries into ${collection.name}"
          data

      }
    }

}