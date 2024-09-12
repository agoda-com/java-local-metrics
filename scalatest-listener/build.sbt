name := "scala-test-metrics"
organization := "io.agodadev"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.15",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.mockito" %% "mockito-scala" % "1.17.12" % Test,
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.12" % Test
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-C", "io.agodadev.testmetricsscala.TestMetricsReporter")