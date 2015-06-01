import scala.concurrent.{ Future, Await }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.backend.DatabasePublisher
import scala.slick.driver.MySQLDriver.api._

object DbConverter extends App {

  val db = Database.forConfig("viscacha")
  try {

    println("Opened Database")

    val usersTable: TableQuery[ViscachaUser] = TableQuery[ViscachaUser]

    val selectUsersAction = usersTable.result.map {
      users => users.foreach { user => println(s"User: $user") }
    }

    val usersFuture = db.run(selectUsersAction)
    
    println("All Done.")
    Await.result(usersFuture, Duration.Inf)

  } finally db.close

}