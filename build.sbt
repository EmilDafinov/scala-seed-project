import Dependencies._

ThisBuild / scalaVersion := "2.13.16"
//DO NOT SET THE VERSION HERE, it is dynamically resolved from git
//ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

val scribeVersion = "3.16.1"
val pekkoVersion = "1.1.0"
val scalaTestVersion = "3.2.19"

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin, AshScriptPlugin)
  .settings(
    name := "Scala Seed Project",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
      "org.apache.pekko" %% "pekko-stream" % "1.1.0",
      "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
      "org.apache.pekko" %% "pekko-http" % pekkoVersion,
      "org.apache.pekko" %% "pekko-connectors-kafka" % "1.0.0",
      "com.outr" %% "scribe" % scribeVersion,
      "org.slf4j" % "slf4j-simple" % "2.0.17",
      "com.typesafe" % "config" % "1.4.3",
      "com.typesafe.slick" %% "slick" % "3.6.0",
      "org.slf4j" % "slf4j-nop" % "1.7.26",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.6.0",
      "org.postgresql" % "postgresql" % "42.7.5",
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % Test,
      "io.circe" %% "circe-core" % "0.15.0-M1",
      "org.scalacheck" %% "scalacheck" % "1.18.1" % Test,
      "org.mdedetrich" %% "pekko-http-circe" % "1.1.0" % pekkoVersion,
    ),
    dockerBaseImage := "amazoncorretto:11-alpine-jdk",
    dockerExposedPorts := Seq(9000, 5005),
    dockerUpdateLatest := true,
    ThisBuild / version ~= (_.replace('+', '_')), //The version is used for the docker tag, it does not like `+` signs
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
