name := "cluster"

organization := "pl.jaca"

version := "1.2.4"

scalaVersion := "2.11.7"

publishMavenStyle := true

crossPaths := false

lazy val publishName = s"server-cluster $version"

publishTo := {
  val path = Path.userHome.absolutePath + "\\Dropbox\\Public\\distributed-server"
  if (version.value.endsWith("SNAPSHOT"))
    Some(Resolver.file(publishName, new File(path + "\\snapshots")))
  else
    Some(Resolver.file(publishName, new File(path + "\\releases")))
}