name := "server-app"

organization := "pl.jaca"

version := "1.0.1"

scalaVersion := "2.11.7"

publishMavenStyle := true

crossPaths := false

lazy val publishName = s"server-app $version"

publishTo := {
  val path = Path.userHome.absolutePath + "\\Dropbox\\Public\\distributed-server"
  if (version.value.endsWith("SNAPSHOT"))
    Some(Resolver.file(publishName, new File(path + "\\snapshots")))
  else
    Some(Resolver.file(publishName, new File(path + "\\releases")))
}