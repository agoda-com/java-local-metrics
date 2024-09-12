name := "scala-test-metrics"
organization := "io.agodadev"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.15",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0"
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-C", "io.agodadev.testmetrics-scala.TestMetricsReporter")