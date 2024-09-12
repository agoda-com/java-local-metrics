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

class TestMetricsReporterSpec extends AnyFunSpec with Matchers with MockitoSugar {
  
  val objectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  describe("TestMetricsReporter") {
    it("should handle test events and send correct JSON report to endpoint") {
      // Mock HTTP request and response
      val mockRequest = mock[HttpRequest]
      val mockResponse = mock[HttpResponse[String]]

      when(mockRequest.postData(any[String])).thenReturn(mockRequest)
      when(mockRequest.header(any[String], any[String])).thenReturn(mockRequest)
      when(mockRequest.option(any)).thenReturn(mockRequest)
      when(mockRequest.asString).thenReturn(mockResponse)
      when(mockResponse.isSuccess).thenReturn(true)

      // Create a TestMetricsReporter with our mocked HTTP client
      val reporter = new TestMetricsReporter {
        override protected def createHttpRequest(url: String): HttpRequest = mockRequest
      }
      
      val runStartingEvent = RunStarting(ordinal = new Ordinal(1), testCount = 3, configMap = ConfigMap.empty, formatter = None, location = None, payload = None, threadName = "main", timeStamp = 123)
      val testStartingEvent = TestStarting(ordinal = new Ordinal(2), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test1", testText = "should do something", formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 124)
      val testSucceededEvent = TestSucceeded(ordinal = new Ordinal(3), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test1", testText = "should do something", recordedEvents = Vector.empty, duration = Some(100), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 224)
      val runCompletedEvent = RunCompleted(ordinal = new Ordinal(4), duration = Some(200), summary = Some(Summary(testsSucceededCount = 1, testsFailedCount = 0, testsIgnoredCount = 0, testsPendingCount = 0, testsCanceledCount = 0, suitesCompletedCount = 1, suitesAbortedCount = 0, scopesPendingCount = 0)), formatter = None, location = None, payload = None, threadName = "main", timeStamp = 324)
      
      reporter(runStartingEvent)
      reporter(testStartingEvent)
      reporter(testSucceededEvent)
      reporter(runCompletedEvent)

      // Verify that HTTP request was made with correct data
      verify(mockRequest).postData(argThat { json: String =>
        val jsonNode = objectMapper.readTree(json)
        jsonNode.get("totalTests").asInt() == 3 &&
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
  }
}