name := "memcached.scala"

version := "0.0.1"

organization := "com.bitlove"

scalaVersion := "2.9.0-1"

parallelExecution in Test := false

libraryDependencies += "org.scala-tools.testing" %% "specs" % "1.6.9-SNAPSHOT" % "test"

resolvers += "Specs Repo" at "http://nexus.scala-tools.org/content/repositories/snapshots"
