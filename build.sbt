import spray.revolver.AppProcess

name := "MMO Server"

version := "0.1"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-remote_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-cluster_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-cluster-metrics_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-contrib_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.0",
  "io.netty" % "netty-all" % "4.0.28.Final",
  "io.reactivex" % "rxscala_2.11" % "0.25.0",
  "io.reactivex" % "rxjava" % "1.0.12",
  "io.kamon" % "sigar-loader" % "1.6.5-rev001",
  "org.scalatest" % "scalatest_2.11" % "3.0.0-M8",
  "junit" % "junit" % "4.12",
  "org.testng" % "testng" % "6.9.6",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"
)

Revolver.settings

cleanupCommands in console :=
  """
    |shutdown()
  """.stripMargin

addCommandAlias("tg-start", "re-start")

addCommandAlias("tg-stop", "re-stop")
