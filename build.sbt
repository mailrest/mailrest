name := """mailrest"""

version := "1.0-SNAPSHOT"

resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.mailrest" % "maildal" % "1.0.0-SNAPSHOT",
  "org.scaldi" %% "scaldi-play" % "0.5.7",
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
  "org.scalatra.scalate" % "scalate-core_2.11" % "1.7.1",
  specs2 % Test,
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  cache,
  ws
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator
