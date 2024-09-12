package io.agodadev.testmetricsscala

import org.scalatest.Reporter
import org.scalatest.events._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scalaj.http.{Http, HttpOptions}

import java.time.Instant
import java.util.UUID
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class TestMetricsReporter extends Reporter {
  private val objectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
  private val testCases = mutable.Map[String, TestCaseInfo]()
  private var suiteStartTime: Instant = _
  private var totalTests: Int = 0
  private var succeededTests: Int = 0
  private var failedTests: Int = 0
  private var ignoredTests: Int = 0

  private val endpointUrl = sys.env.getOrElse("BUILD_METRICS_ES_ENDPOINT", "http://compilation-metrics/scalatest")

  case class TestCaseInfo(
    id: String,
    name: String,
    suiteName: String,
    status: String,
    startTime: Instant,
    endTime: Instant = Instant.now(),
    duration: Long = 0
  )

  override def apply(event: Event): Unit = event match {
    case RunStarting(ordinal, testCount, configMap, formatter, location, payload, threadName, timeStamp) =>
      suiteStartTime = Instant.now()
      totalTests = testCount

    case TestStarting(ordinal, suiteName, suiteId, suiteClassName, testName, testText, formatter, location, rerunner, payload, threadName, timeStamp) =>
      val testId = UUID.randomUUID().toString
      testCases += (testId -> TestCaseInfo(testId, testName, suiteName, "Started", Instant.now()))

    case TestSucceeded(ordinal, suiteName, suiteId, suiteClassName, testName, testText, recordedEvents, duration, formatter, location, rerunner, payload, threadName, timeStamp) =>
      updateTestCase(testName, "Passed", duration)
      succeededTests += 1

    case TestFailed(ordinal, message, suiteName, suiteId, suiteClassName, testName, testText, recordedEvents, analysis, throwable, duration, formatter, location, rerunner, payload, threadName, timeStamp) =>
      updateTestCase(testName, "Failed", duration)
      failedTests += 1

    case TestIgnored(ordinal, suiteName, suiteId, suiteClassName, testName, testText, formatter, location, payload, threadName, timeStamp) =>
      val testId = UUID.randomUUID().toString
      testCases += (testId -> TestCaseInfo(testId, testName, suiteName, "Ignored", Instant.now()))
      ignoredTests += 1

    case RunCompleted(ordinal, duration, summary, formatter, location, payload, threadName, timeStamp) =>
      val jsonReport = generateJsonReport(summary.getOrElse(Summary(0, 0, 0, 0, 0, 0, 0, 0)), duration)
      sendReportToEndpoint(jsonReport)

    case _ => // Ignore other events
  }

  private def updateTestCase(testName: String, status: String, duration: Option[Long]): Unit = {
    testCases.find(_._2.name == testName).foreach { case (id, testCase) =>
      testCases(id) = testCase.copy(
        status = status,
        endTime = Instant.now(),
        duration = duration.getOrElse(0)
      )
    }
  }

  private def generateJsonReport(summary: Summary, duration: Option[Long]): ObjectNode = {
    val rootNode = objectMapper.createObjectNode()
    rootNode.put("id", UUID.randomUUID().toString)
    rootNode.put("userName", System.getProperty("user.name"))
    rootNode.put("cpuCount", Runtime.getRuntime().availableProcessors())
    rootNode.put("hostname", java.net.InetAddress.getLocalHost.getHostName)
    rootNode.put("os", s"${System.getProperty("os.name")} ${System.getProperty("os.version")}")
    rootNode.put("platform", determinePlatform())
    rootNode.put("projectName", "YourProjectName") // You might want to make this configurable
    rootNode.put("isDebuggerAttached", java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.toString.contains("-agentlib:jdwp"))

    val testCasesNode = rootNode.putArray("scalaTestCases")
    testCases.values.foreach { testCase =>
      val testCaseNode = testCasesNode.addObject()
      testCaseNode.put("id", testCase.id)
      testCaseNode.put("name", testCase.name)
      testCaseNode.put("suiteName", testCase.suiteName)
      testCaseNode.put("status", testCase.status)
      testCaseNode.put("startTime", testCase.startTime.toString)
      testCaseNode.put("endTime", testCase.endTime.toString)
      testCaseNode.put("duration", testCase.duration)
    }

    rootNode.put("totalTests", totalTests)
    rootNode.put("succeededTests", succeededTests)
    rootNode.put("failedTests", failedTests)
    rootNode.put("ignoredTests", ignoredTests)
    rootNode.put("pendingTests", summary.testsPendingCount)
    rootNode.put("canceledTests", summary.testsCanceledCount)
    rootNode.put("completedSuites", summary.suitesCompletedCount)
    rootNode.put("abortedSuites", summary.suitesAbortedCount)
    rootNode.put("pendingScopes", summary.scopesPendingCount)
    rootNode.put("runTime", duration.getOrElse(0L))
    rootNode.put("runId", UUID.randomUUID().toString)

    rootNode
  }

  private def sendReportToEndpoint(jsonReport: ObjectNode): Unit = {
    val jsonString = objectMapper.writeValueAsString(jsonReport)
    Try {
      val response = Http(endpointUrl)
        .postData(jsonString)
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000))
        .asString
      if (response.isSuccess) {
        println(s"Successfully sent report to $endpointUrl")
      } else {
        println(s"Failed to send report. Status code: ${response.code}, Body: ${response.body}")
      }
    } match {
      case Success(_) => // Do nothing, already logged
      case Failure(exception) => println(s"Exception when sending report: ${exception.getMessage}")
    }
  }

  private def determinePlatform(): String = {
    if (System.getProperty("java.vendor").contains("Android")) "Android"
    else if (System.getenv("DOCKER_CONTAINER") != null) "Docker"
    else if (System.getenv("AWS_EXECUTION_ENV") != null) "AWS"
    else "JVM"
  }
}