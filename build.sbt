name := """fnb-db-converter"""

mainClass in Compile := Some("app.DbConverter")

scalaVersion := "2.11.7"

lazy val fnbDatamodel = RootProject(file("../FridayNightBeer/modules/datamodel"))

lazy val fnbConverter = (project in file(".")).dependsOn(fnbDatamodel)

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "commons-lang" % "commons-lang" % "2.6",
  "ch.qos.logback" % "logback-classic" % "1.1.1",
  "mysql" % "mysql-connector-java" % "latest.release",
  "org.reactivemongo" %% "reactivemongo" % "0.11.4",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

EclipseKeys.withSource := true

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions ++= Seq("-feature", "-deprecation")
