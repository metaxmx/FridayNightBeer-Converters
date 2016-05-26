import sbt.RootProject

name := """fnb-db-converter"""

mainClass in Compile := Some("app.DbConverter")

scalaVersion := "2.11.8"

lazy val fnbPlay = RootProject(file("../FridayNightBeer"))

lazy val fnbConverter = (project in file("."))
  .dependsOn(fnbPlay)


resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "commons-lang" % "commons-lang" % "2.6",
  "ch.qos.logback" % "logback-classic" % "1.1.1",
  "mysql" % "mysql-connector-java" % "5.1.27",
  "org.reactivemongo" %% "reactivemongo" % "0.11.11",
  "joda-time" % "joda-time" % "2.8.1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

dependencyOverrides ++= Set(
  "org.webjars.npm" % "minimatch" % "3.0.0",
  "org.webjars.npm" % "glob" % "7.0.3"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions ++= Seq("-feature", "-deprecation")
