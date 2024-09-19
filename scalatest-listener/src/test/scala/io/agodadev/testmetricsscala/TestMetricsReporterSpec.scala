package io.agodadev.testmetricsscala
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.mockito.ArgumentMatchers.{any, argThat}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.MockitoSugar
import org.scalatest.events._
import scalaj.http.{HttpRequest, HttpResponse}
import scalaj.http.HttpOptions.HttpOption

// Use scala.collection.JavaConverters for Scala 2.12 compatibility
import scala.collection.JavaConverters._

class TestMetricsReporterSpec extends AnyFunSpec with Matchers with MockitoSugar {

  val objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.module.scala.DefaultScalaModule)

  describe("TestMetricsReporter") {
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
      val test1SucceededEvent = TestSucceeded(ordinal = new Ordinal(3), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test1", testText = "should do something", recordedEvents = Vector.empty[RecordableEvent], duration = Some(50), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 151)
      val test2StartingEvent = TestStarting(ordinal = new Ordinal(4), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test2", testText = "should do something else", formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 152)
      val test2FailedEvent = TestFailed(ordinal = new Ordinal(5), message = "assertion failed", suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), testName = "test2", testText = "should do something else", recordedEvents = Vector.empty[RecordableEvent], analysis = Vector.empty[String], throwable = None, duration = Some(75), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 227)
      val suiteCompletedEvent = SuiteCompleted(ordinal = new Ordinal(6), suiteName = "TestSuite", suiteId = "suiteId", suiteClassName = Some("TestSuite"), duration = Some(150), formatter = None, location = None, rerunner = None, payload = None, threadName = "main", timeStamp = 250)

      reporter(suiteStartingEvent)
      reporter(test1StartingEvent)
      reporter(test1SucceededEvent)
      reporter(test2StartingEvent)
      reporter(test2FailedEvent)
      reporter(suiteCompletedEvent)

      verify(mockRequest).postData(argThat { json: String =>
        val jsonNode = objectMapper.readTree(json)
        println(s"Received JSON: ${objectMapper.writeValueAsString(jsonNode)}")
        val result = validateJsonContent(jsonNode)
        if (!result) {
          println("JSON validation failed. Details:")
          println(s"suiteName: ${jsonNode.get("suiteName").asText()}")
          println(s"totalTests: ${jsonNode.get("totalTests").asInt()}")
          println(s"succeededTests: ${jsonNode.get("succeededTests").asInt()}")
          println(s"failedTests: ${jsonNode.get("failedTests").asInt()}")
          println(s"ignoredTests: ${jsonNode.get("ignoredTests").asInt()}")
          println(s"runTime: ${jsonNode.get("runTime").asLong()}")
          println(s"scalaTestCases: ${jsonNode.get("scalaTestCases").toString}")
        }
        result
      })
      verify(mockRequest).asString
      verify(mockResponse).isSuccess
    }
  }

  def validateJsonContent(jsonNode: JsonNode): Boolean = {
    val testCases = jsonNode.get("scalaTestCases")

    jsonNode.get("suiteName").asText() == "TestSuite" &&
      jsonNode.get("totalTests").asInt() == 2 &&
      jsonNode.get("succeededTests").asInt() == 1 &&
      jsonNode.get("failedTests").asInt() == 1 &&
      jsonNode.get("ignoredTests").asInt() == 0 &&
      jsonNode.get("runTime").asLong() == 150 &&
      testCases.isArray &&
      testCases.size() == 2 &&
      testCasesContain(testCases, "test1", "Passed") &&
      testCasesContain(testCases, "test2", "Failed")
  }

  def testCasesContain(testCases: JsonNode, name: String, status: String): Boolean = {
    // Use JavaConverters for Scala 2.12 compatibility
    testCases.asScala.exists(tc => tc.get("name").asText() == name && tc.get("status").asText() == status)
  }
}