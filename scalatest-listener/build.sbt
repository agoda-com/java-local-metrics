import xerial.sbt.Sonatype._

name := "scala-test-metrics"
organization := "io.agodadev"

// Cross-compile settings
crossScalaVersions := Seq("2.12.19", "2.13.12", "2.13.11")
scalaVersion := "2.13.12"

version := sys.env.get("BUNDLE_VERSION_NUMBER").getOrElse("0.1.0")
versionScheme := Some("early-semver")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.15",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.mockito" %% "mockito-scala" % "1.17.12" % Test,
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.12" % Test
)

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-C", "io.agodadev.testmetricsscala.TestMetricsReporter")

// Maven Central publishing settings
publishMavenStyle := true
publishTo := sonatypePublishToBundle.value
sonatypeProjectHosting := Some(GitHubHosting("agoda-com", "scala-test-metrics", "maven@agoda.com"))
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/agoda-com/java-local-metrics"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/agoda-com/java-local-metrics"),
    "scm:git@github.com:agoda-com/java-local-metrics.git"
  )
)
developers := List(
  Developer(
    id    = "joeldickson",
    name  = "Joel Dickson",
    email = "maven@agoda.com",
    url   = url("http://beerandserversdontmix.com")
  )
)

// PGP signing settings
useGpgPinentry := true
pgpPassphrase := sys.env.get("GPG_PASSPHRASE").map(_.toArray)

// Add these settings for cross-building
crossPaths := true
publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)

// Ensure that the Sonatype bundle includes artifacts for all Scala versions
sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / "target" / "sonatype-staging"
