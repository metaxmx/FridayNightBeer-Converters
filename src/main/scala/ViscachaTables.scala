import slick.driver.MySQLDriver.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}

class ViscachaUser(tag: Tag)
  extends Table[(Int, String, String, String, Long, String, String, String, String, Option[String], String, Long)](tag, "v_user") {

  // This is the primary key column:
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey)
  def name: Rep[String] = column[String]("name")
  def pw: Rep[String] = column[String]("pw")
  def mail: Rep[String] = column[String]("mail")
  def regdate: Rep[Long] = column[Long]("regdate")
  def fullname: Rep[String] = column[String]("fullname")
  def signature: Rep[String] = column[String]("signature")
  def location: Rep[String] = column[String]("location")
  def gender: Rep[String] = column[String]("gender")
  def birthday: Rep[Option[String]] = column[Option[String]]("birthday")
  def pic: Rep[String] = column[String]("pic")
  def lastvisit: Rep[Long] = column[Long]("lastvisit")
  
  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Int, String, String, String, Long, String, String, String, String, Option[String], String, Long)] =
    (id, name, pw, mail, regdate, fullname, signature, location, gender, birthday, pic, lastvisit)
}