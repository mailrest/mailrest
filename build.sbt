name := """restapi"""

version := "1.0-SNAPSHOT"

resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.mailrest" % "maildal" % "1.0.0-SNAPSHOT",
  "org.scaldi" %% "scaldi-play" % "0.5.7",
  "com.typesafe" % "config" % "1.3.0",
  cache,
  ws
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator
