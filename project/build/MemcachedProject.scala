import sbt._
import Process._
import java.io.File
import java.lang.ProcessBuilder

class MemcachedProject(info: ProjectInfo) extends DefaultProject(info) {
  val specsRepo = "Specs Repo" at "http://nexus.scala-tools.org/content/repositories/snapshots"
  val specs     = "org.scala-tools.testing" %% "specs" % "1.6.6"
}
