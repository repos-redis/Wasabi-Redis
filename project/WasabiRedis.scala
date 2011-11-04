import sbt._
import Keys._


object Resolvers {
  val javanet = "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
}

object Dependencies {

  val jedis = "redis.clients" % "jedis" % "2.0.0" withSources()

  val testDeps = Seq(
    "junit" % "junit" % "4.7" % "test" withSources(),
    "org.scala-tools.testing" %% "specs" % "1.6.8" % "test" withSources()
  )
}

object WasabiRedis extends Build {

  import Resolvers._
  import Dependencies._

  val _version = "0.1-SNAPSHOT"
  val _organization = "org.wasabiredis"

  lazy val core = Project("wasabiredis", file("."), settings = Defaults.defaultSettings ++ Seq(
    name := "wasabiredis",
    version := _version,
    organization := _organization,
    resolvers := Seq(javanet),
    libraryDependencies ++= Seq(jedis) ++ testDeps))
}