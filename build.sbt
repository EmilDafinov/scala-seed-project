import Dependencies._

ThisBuild / scalaVersion := "2.13.16"
//DO NOT SET THE VERSION HERE, it is dynamically resolved from git
//ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

val scribeVersion = "3.16.1"
val pekkoVersion = "1.1.0"

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin, AshScriptPlugin)
  .settings(
    name := "Scala Seed Project",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
      "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
      "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
      "org.apache.pekko" %% "pekko-http" % pekkoVersion,
      "com.outr" %% "scribe" % scribeVersion,
      "org.slf4j" % "slf4j-simple" % "2.0.17",
      munit % Test,
    ),
    dockerBaseImage := "openjdk:8-jdk-slim",
    dockerUpdateLatest := true,
    ThisBuild / version ~= (_.replace('+', '_')), //The version is used for the docker tag, it does not like `+` signs
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
