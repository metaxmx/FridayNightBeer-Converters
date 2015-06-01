import scala.concurrent.{ Future, Await }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.backend.DatabasePublisher
import scala.slick.driver.MySQLDriver.api._
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONObjectID
import scala.util.parsing.json.JSONType
import reactivemongo.bson.BSON
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONArray
import play.api.libs.iteratee.Enumerator
import reactivemongo.bson.BSONInteger
import reactivemongo.bson.BSONString
import scala.concurrent._

object DbConverter extends App {

  val viscachaDb = Database.forConfig("viscacha")

  val mongoDriver = new MongoDriver
  val mongoConnection = mongoDriver.connection(List("localhost"))
  val mongoDb = mongoConnection.db("fnb")

  val fnbUsercollection = mongoDb.collection[BSONCollection]("users")

  try {

    println("Opened Database")

    val usersTable: TableQuery[ViscachaUsers] = TableQuery[ViscachaUsers]

    val selectUsersAction = usersTable.result.map {
      users =>
        val vUsers = users.map {
          user =>
            println(s"Found: $user")
            FnbUser(BSONObjectID.generate, user.name, user.pw, user.name, user.fullname)
        }
        val fnbUserArray = BSONArray(vUsers)
        val docs = (18 to 60).toStream.map(i => BSONDocument("age" -> BSONInteger(i), "name" -> BSONString("Jack" + i)))
        val x = Stream.empty[BSONDocument]
        // First Clear All
        fnbUsercollection.remove(BSONDocument())
          // Then insert all FnbUsers
          .flatMap {
            lastError =>
              vUsers.foreach { user => println(s"Insert: $user") }
              fnbUsercollection.bulkInsert(Enumerator.enumerate(vUsers))
          }

    }

    val usersFuture = viscachaDb.run(selectUsersAction)
    Await.result(usersFuture, Duration.Inf)

    println("All Done.")

  } finally viscachaDb.close

}