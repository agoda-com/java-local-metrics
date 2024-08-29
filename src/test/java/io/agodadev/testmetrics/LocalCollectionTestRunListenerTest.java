package io.agodadev.testmetrics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalCollectionTestRunListenerTest {

    private HttpClient mockHttpClient;
    private LocalCollectionTestRunListener listener;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        listener = new LocalCollectionTestRunListener() {
            @Override
            protected HttpClient createHttpClient() {
                return mockHttpClient;
            }
        };

        objectMapper = new ObjectMapper();
    }

    @Test
    public void testRunFinishedSendsCorrectData() throws Exception {
        Description description1 = Description.createTestDescription(TestClass.class, "testMethod1");
        Description description2 = Description.createTestDescription(TestClass.class, "testMethod2");

        listener.testRunStarted(Description.createSuiteDescription(TestClass.class));
        listener.testStarted(description1);
        listener.testFinished(description1);
        listener.testStarted(description2);
        listener.testFailure(new Failure(description2, new AssertionError("Test failed")));
        listener.testFinished(description2);

        Result result = new Result();
        listener.testRunFinished(result);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("http://compilation-metrics/junit", capturedRequest.uri().toString());
        assertEquals("POST", capturedRequest.method());
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").orElse(null));

        JsonNode rootNode = HttpRequestBodyParser.parseBody(capturedRequest);

        assertNotNull(rootNode.get("id"));
        assertNotNull(rootNode.get("userName"));
        assertTrue(rootNode.get("cpuCount").asInt() > 0);
        assertNotNull(rootNode.get("hostname"));
        assertNotNull(rootNode.get("os"));
        assertNotNull(rootNode.get("projectName"));
        assertNotNull(rootNode.get("isDebuggerAttached"));
        assertNotNull(rootNode.get("runId"));

        JsonNode testCasesNode = rootNode.get("jUnitTestCases");
        assertTrue(testCasesNode.isArray());
        assertEquals(2, testCasesNode.size());

        JsonNode testCase1 = testCasesNode.get(1);
        assertEquals("Passed", testCase1.get("result").asText());
        assertEquals("testMethod1", testCase1.get("methodname").asText());

        JsonNode testCase2 = testCasesNode.get(0);
        assertEquals("Failed", testCase2.get("result").asText());
        assertEquals("testMethod2", testCase2.get("methodname").asText());
    }

    private static class TestClass {
        @Test
        public void testMethod1() {}

        @Test
        public void testMethod2() {}
    }

}