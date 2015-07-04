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

      try {

        val process = Future.successful(ViscachaForumData()) flatMap {
          fetchData[ViscachaUser]("Users", fetchViscachaUsers, (data, entities) => data withUsers entities)
        } flatMap {
          fetchData[ViscachaGroup]("Groups", fetchViscachaGroups, (data, entities) => data withGroups entities)
        } flatMap {
          fetchData[ViscachaCategory]("Categories", fetchViscachaCategories, (data, entities) => data withCategories entities)
        } flatMap {
          fetchData[ViscachaForum]("Forums", fetchViscachaForums, (data, entities) => data withForums entities)
        } flatMap {
          fetchData[ViscachaTopic]("Topics", fetchViscachaTopics, (data, entities) => data withTopics entities)
        } flatMap {
          fetchData[ViscachaReply]("Replies", fetchViscachaReplies, (data, entities) => data withReplies entities)
        } flatMap {
          fetchData[ViscachaForumPermission]("Forum Permissions", fetchViscachaForumPermissions, (data, entities) => data withForumPermission entities)
        } map {
          new AggregateData(_).aggregate
        } flatMap {
          insertData(User.collectionName) { _.users }
        } flatMap {
          insertData(ForumCategory.collectionName) { _.categories }
        } flatMap {
          insertData(Forum.collectionName) { _.forums }
        } flatMap {
          insertData(Thread.collectionName) { _.threads }
        } flatMap {
          insertData(Post.collectionName) { _.posts }
        }

        Await.result(process, Duration.Inf)

      } finally {
        mongoConnection.close
        mongoConnection.actorSystem.shutdown()
      }

    } finally viscachaDb.close

    logger.info("All Done.")

  }

  def fetchViscachaUsers(implicit db: Database) = db.run(TableQuery[ViscachaUsers].result)

  def fetchViscachaGroups(implicit db: Database) = db.run(TableQuery[ViscachaGroups].result)

  def fetchViscachaCategories(implicit db: Database) = db.run(TableQuery[ViscachaCategories].sortBy { _.position }.result)

  def fetchViscachaForums(implicit db: Database) = db.run(TableQuery[ViscachaForums].sortBy { _.position }.result)

  def fetchViscachaTopics(implicit db: Database) = db.run(TableQuery[ViscachaTopics].sortBy { _.id }.result)

  def fetchViscachaReplies(implicit db: Database) = db.run(TableQuery[ViscachaReplies].sortBy { _.id }.result)

  def fetchViscachaForumPermissions(implicit db: Database) = db.run(TableQuery[ViscachaForumPermissions].sortBy { _.id }.result)

  def fetchData[T](typeName: String, fetch: => Future[Seq[T]], aggregate: (ViscachaForumData, Seq[T]) => ViscachaForumData): ViscachaForumData => Future[ViscachaForumData] =
    data =>
      fetch map {
        entities =>
          logger info s"Fetched ${entities.size} $typeName"
          aggregate(data, entities)
      }

  def insertData[T](collectionName: String)(resolve: FnbForumData => Seq[T])(implicit db: DB, writer: BSONDocumentWriter[T]): FnbForumData => Future[FnbForumData] =
    data => {
      val collection = db.collection[BSONCollection](collectionName)
      val entities = resolve(data).map(writer.write(_)).toStream
      collection.remove(BSONDocument()).flatMap {
        lastError =>
          collection.bulkInsert(entities, true)
      } map {
        case MultiBulkWriteResult(ok, inserts, _, _, _, _, _, _, _) =>
          logger info s"Inserted $inserts entries into ${collection.name}"
          data
      }
    }

}