package io.agodadev.testmetricsscala

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.events._
import org.scalatest.ConfigMap
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.{any, argThat}
import scalaj.http.{HttpRequest, HttpResponse}
import scalaj.http.HttpOptions.HttpOption

class TestMetricsReporterSpec extends AnyFunSpec with Matchers with MockitoSugar {

  val objectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  describe("TestMetricsReporter") {
    it("should handle test events and send correct JSON report to endpoint for each suite") {
      // Mock HTTP request and response
      val mockRequest = mock[HttpRequest]
      val mockResponse = mock[HttpResponse[String]]

      when(mockRequest.postData(any[String])).thenReturn(mockRequest)
      when(mockRequest.header(any[String], any[String])).thenReturn(mockRequest)
      when(mockRequest.option(any[HttpOption]())).thenReturn(mockRequest)
      when(mockRequest.asString).thenReturn(mockResponse)
      when(mockResponse.isSuccess).thenReturn(true)

      // Create a TestMetricsReporter with our mocked HTTP client
      val reporter = new TestMetricsReporter {
        override protected def createHttpRequest(url: String): HttpRequest = mockRequest
      }

      val runStartingEvent = RunStarting(ordinal = new Ordinal(1), testCount = 3, configMap = ConfigMap.empty, formatter = None, location = None, payload = None, threadName = "main", timeStamp = 123)
      val suiteStartingEvent = SuiteStarting(ordinal = new Ordinal(2), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 124)
      val testStartingEvent = TestStarting(ordinal = new Ordinal(3), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test1", testText = "should do something", formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 125)
      val testSucceededEvent = TestSucceeded(ordinal = new Ordinal(4), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test1", testText = "should do something", recordedEvents = Vector.empty, duration = Some(100), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 225)
      val suiteCompletedEvent = SuiteCompleted(ordinal = new Ordinal(5), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), duration = Some(200), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 325)

      reporter(runStartingEvent)
      reporter(suiteStartingEvent)
      reporter(testStartingEvent)
      reporter(testSucceededEvent)
      reporter(suiteCompletedEvent)

      // Verify that HTTP request was made with correct data
      verify(mockRequest).postData(argThat { json: String =>
        val jsonNode = objectMapper.readTree(json)
        jsonNode.get("suiteName").asText() == "TestSuite" &&
          jsonNode.get("totalTests").asInt() == 1 &&
          jsonNode.get("succeededTests").asInt() == 1 &&
          jsonNode.get("failedTests").asInt() == 0 &&
          jsonNode.get("ignoredTests").asInt() == 0 &&
          jsonNode.get("runTime").asLong() == 200 &&
          jsonNode.get("scalaTestCases").isArray &&
          jsonNode.get("scalaTestCases").size() == 1 &&
          jsonNode.get("scalaTestCases").get(0).get("name").asText() == "test1" &&
          jsonNode.get("scalaTestCases").get(0).get("status").asText() == "Passed"
      })
      verify(mockRequest).asString
      verify(mockResponse).isSuccess
    }

    it("should handle multiple test cases within a suite") {
      val mockRequest = mock[HttpRequest]
      val mockResponse = mock[HttpResponse[String]]

      when(mockRequest.postData(any[String])).thenReturn(mockRequest)
      when(mockRequest.header(any[String], any[String])).thenReturn(mockRequest)
      when(mockRequest.option(any[HttpOption]())).thenReturn(mockRequest)
      when(mockRequest.asString).thenReturn(mockResponse)
      when(mockResponse.isSuccess).thenReturn(true)

      val reporter = new TestMetricsReporter {
        override protected def createHttpRequest(url: String): HttpRequest = mockRequest
      }

      val suiteStartingEvent = SuiteStarting(ordinal = new Ordinal(1), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 100)
      val test1StartingEvent = TestStarting(ordinal = new Ordinal(2), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test1", testText = "should do something", formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 101)
      val test1SucceededEvent = TestSucceeded(ordinal = new Ordinal(3), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test1", testText = "should do something", recordedEvents = Vector.empty, duration = Some(50), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 151)
      val test2StartingEvent = TestStarting(ordinal = new Ordinal(4), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test2", testText = "should do something else", formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 152)
      val test2FailedEvent = TestFailed(
        ordinal = new Ordinal(5),
        message = "assertion failed",
        suiteName = "TestSuite",
        suiteId = "suiteId",
        suiteClassName = Some("TestSuite"),
        testName = "test2",
        testText = "should do something else",
        recordedEvents = Vector.empty[RecordableEvent], // Changed to RecordableEvent
        analysis = Vector.empty[String], // Changed to Vector.empty[String]
        throwable = None,
        duration = Some(75),
        formatter = None,
        location = None,
        rerunner = None,
        payload = None,
        threadName = "main",
        timeStamp = 227
      )
      val suiteCompletedEvent = SuiteCompleted(ordinal = new Ordinal(6), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), duration = Some(150), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 250)

      reporter(suiteStartingEvent)
      reporter(test1StartingEvent)
      reporter(test1SucceededEvent)
      reporter(test2StartingEvent)
      reporter(test2FailedEvent)
      reporter(suiteCompletedEvent)

      verify(mockRequest).postData(argThat { json: String =>
        val jsonNode = objectMapper.readTree(json)
        jsonNode.get("suiteName").asText() == "TestSuite" &&
          jsonNode.get("totalTests").asInt() == 2 &&
          jsonNode.get("succeededTests").asInt() == 1 &&
          jsonNode.get("failedTests").asInt() == 1 &&
          jsonNode.get("ignoredTests").asInt() == 0 &&
          jsonNode.get("runTime").asLong() == 150 &&
          jsonNode.get("scalaTestCases").isArray &&
          jsonNode.get("scalaTestCases").size() == 2 &&
          jsonNode.get("scalaTestCases").get(0).get("name").asText() == "test1" &&
          jsonNode.get("scalaTestCases").get(0).get("status").asText() == "Passed" &&
          jsonNode.get("scalaTestCases").get(1).get("name").asText() == "test2" &&
          jsonNode.get("scalaTestCases").get(1).get("status").asText() == "Failed"
      })
      verify(mockRequest).asString
      verify(mockResponse).isSuccess
    }
  }
}