package io.agodadev.testmetricsscala

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.events._
import org.scalatest.ConfigMap
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.Instant
import scala.jdk.CollectionConverters._

class TestMetricsReporterSpec extends AnyFunSpec with Matchers {

  val objectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  describe("TestMetricsReporter") {
    it("should handle test events and generate correct JSON report") {
      val reporter = new TestMetricsReporter()

      // Capture all output from the reporter
      val baos = new ByteArrayOutputStream()
      Console.withOut(new PrintStream(baos)) {
        val runStartingEvent = RunStarting(new Ordinal(1), 3, ConfigMap.empty, None, None, None, "main", 123)
        val testStartingEvent = TestStarting(new Ordinal(2), "TestSuite", "suiteId", Some("TestSuite"), "test1", "should do something", None, None, None, None, "main", 124)
        val testSucceededEvent = TestSucceeded(new Ordinal(3), "TestSuite", "suiteId", Some("TestSuite"), "test1", "should do something", Vector.empty, Some(100), None, None, None, None, "main", 224)
        val runCompletedEvent = RunCompleted(new Ordinal(4), Some(200), Some(Summary(1, 0, 0, 0, 0, 1, 0, 0)), None, None, None, "main", 324)

        reporter(runStartingEvent)
        reporter(testStartingEvent)
        reporter(testSucceededEvent)
        reporter(runCompletedEvent)
      }

      val jsonReport = objectMapper.readTree(baos.toString)

      println("Full JSON report:")
      println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonReport))

      def safeGetIntWithDebug(node: JsonNode, field: String): Option[Int] = {
        val result = Option(node.get(field)).map(_.asInt())
        println(s"Debug: Field '$field' value is $result")
        result
      }

      safeGetText(jsonReport, "userName") shouldBe Some(System.getProperty("user.name"))
      safeGetInt(jsonReport, "cpuCount") shouldBe Some(Runtime.getRuntime().availableProcessors())
      safeGetText(jsonReport, "hostname") should not be empty
      safeGetText(jsonReport, "os") should not be empty
      safeGetText(jsonReport, "platform") shouldBe Some("JVM")
      safeGetBoolean(jsonReport, "isDebuggerAttached") shouldBe Some(false)

      val testCasesNode = jsonReport.get("scalaTestCases")
      testCasesNode should not be null
      testCasesNode.isArray shouldBe true
      testCasesNode.size() shouldBe 1

      val testCase = testCasesNode.get(0)
      safeGetText(testCase, "name") shouldBe Some("test1")
      safeGetText(testCase, "suiteName") shouldBe Some("TestSuite")
      safeGetText(testCase, "status") shouldBe Some("Passed")
      safeGetInt(testCase, "duration") shouldBe Some(100)

      safeGetIntWithDebug(jsonReport, "totalTests") shouldBe Some(3)
      safeGetIntWithDebug(jsonReport, "succeededTests") shouldBe Some(1)
      safeGetIntWithDebug(jsonReport, "failedTests") shouldBe Some(0)
      safeGetIntWithDebug(jsonReport, "ignoredTests") shouldBe Some(0)
      safeGetIntWithDebug(jsonReport, "pendingTests") shouldBe Some(0)
      safeGetIntWithDebug(jsonReport, "canceledTests") shouldBe Some(0)
      safeGetIntWithDebug(jsonReport, "completedSuites") shouldBe Some(1)
      safeGetIntWithDebug(jsonReport, "abortedSuites") shouldBe Some(0)
      safeGetIntWithDebug(jsonReport, "pendingScopes") shouldBe Some(0)
      safeGetIntWithDebug(jsonReport, "runTime") shouldBe Some(200)
    }
  }

  // Helper functions remain the same
  def safeGetInt(node: JsonNode, field: String): Option[Int] =
    Option(node.get(field)).map(_.asInt())

  def safeGetBoolean(node: JsonNode, field: String): Option[Boolean] =
    Option(node.get(field)).map(_.asBoolean())

  def safeGetText(node: JsonNode, field: String): Option[String] =
    Option(node.get(field)).map(_.asText())
}