name := "distributed-server"

organization := "pl.jaca"

version := "1.2.3"

scalaVersion := "2.11.7"

scalaBinaryVersion := CrossVersion.binaryScalaVersion("2.11.7")

crossPaths := false


lazy val commonDependencies = Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-remote_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-cluster_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-contrib_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.0",
  "io.netty" % "netty-all" % "4.1.0.Beta5",
  "io.reactivex" % "rxscala_2.11" % "0.25.0",
  "io.reactivex" % "rxjava" % "1.0.12",
  "io.kamon" % "sigar-loader" % "1.6.5-rev001",
  "org.scalatest" % "scalatest_2.11" % "3.0.0-M8",
  "junit" % "junit" % "4.12",
  "org.testng" % "testng" % "6.9.6",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0-M1",
  "com.google.guava" % "guava" % "18.0",
  "mysql" % "mysql-connector-java" % "5.1.38",
  "com.typesafe.slick" % "slick-hikaricp_2.11" % "3.1.1",
  "com.typesafe.slick" % "slick_2.11" % "3.1.1"
)

lazy val serverApp = Project(
  id = "app",
  base = file("app"),
  settings = Seq(
    libraryDependencies ++= commonDependencies
  )) dependsOn(cluster, common)

lazy val cluster = Project(id = "cluster",
  base = file("cluster"),
  settings = Seq(
    libraryDependencies ++= commonDependencies
  )) dependsOn common

lazy val common = Project(id = "common",
  base = file("common"),
  settings = Seq(
    libraryDependencies ++= commonDependencies
  ))

lazy val examples = Project(id = "examples",
  base = file("examples"),
  settings = Seq(
    libraryDependencies ++= commonDependencies,
    publishLocal := {},
    publish := {}
  )) dependsOn(cluster, serverApp, common)

publishLocal := {}

publish := {}

cleanupCommands in console :=
  """
    |shutdown()
  """.stripMargin

addCommandAlias("tg-start", "re-start")

addCommandAlias("tg-stop", "re-stop")

addCommandAlias("publishm8", "publishM2")