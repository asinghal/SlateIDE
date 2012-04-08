import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  lazy val SCALA_VERSION = "2.8.1" // "2.9.1"

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "slate",
      organization := "net.slate",
      version := "0.3",
      scalaVersion := SCALA_VERSION,
      libraryDependencies += "org.scala-lang" % "scala-swing" % SCALA_VERSION,
      libraryDependencies += "org.scala-lang" % "scala-compiler" % SCALA_VERSION,
      libraryDependencies += "org.scala-lang" % "scala-library" % SCALA_VERSION
    )
  )
}
