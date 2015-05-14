name := """restapi"""

version := "1.0-SNAPSHOT"

resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/maven.repo"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.noorq.casser" % "casser-core" % "1.0.0",
  jdbc,
  anorm,
  cache,
  ws
)

