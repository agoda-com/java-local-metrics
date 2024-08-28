package io.agodadev.testmetrics;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.InetAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class LocalCollectionTestRunListener extends RunListener {
    private final String apiEndpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<Description, TestCaseInfo> testCases;
    private Instant suiteStartTime;

    public LocalCollectionTestRunListener() {
        this.apiEndpoint = getApiEndpoint("http://compilation-metrics/junit");
        this.httpClient = createHttpClient();
        this.objectMapper = new ObjectMapper();
        this.testCases = new HashMap<>();
    }

    private String getApiEndpoint(String defaultEndpoint) {
        String envEndpoint = System.getenv("BUILD_METRICS_ES_ENDPOINT");
        return (envEndpoint != null && !envEndpoint.isEmpty()) ? envEndpoint : defaultEndpoint;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        suiteStartTime = Instant.now();
    }

    @Override
    public void testStarted(Description description) throws Exception {
        testCases.put(description, new TestCaseInfo(description, Instant.now()));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        TestCaseInfo testCase = testCases.get(description);
        if (testCase != null) {
            testCase.endTime = Instant.now();
            testCase.duration = Duration.between(testCase.startTime, testCase.endTime).toMillis();
            testCase.result = "Passed";
        }
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        TestCaseInfo testCase = testCases.get(failure.getDescription());
        if (testCase != null) {
            testCase.result = "Failed";
        }
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        TestCaseInfo testCase = testCases.get(failure.getDescription());
        if (testCase != null) {
            testCase.result = "Skipped";
        }
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        testCases.put(description, new TestCaseInfo(description, Instant.now(), "Ignored"));
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("id", UUID.randomUUID().toString());
        rootNode.put("userName", System.getProperty("user.name"));
        rootNode.put("cpuCount", Runtime.getRuntime().availableProcessors());
        rootNode.put("hostname", InetAddress.getLocalHost().getHostName());
        rootNode.put("os", System.getProperty("os.name"));
        rootNode.put("projectName", System.getProperty("user.dir").substring(System.getProperty("user.dir").lastIndexOf('/') + 1));
        rootNode.put("isDebuggerAttached", java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0);
        
        ArrayNode testCasesArray = rootNode.putArray("jUnitTestCases");
        for (TestCaseInfo testCase : testCases.values()) {
            ObjectNode testCaseNode = testCasesArray.addObject();
            testCaseNode.put("id", testCase.id);
            testCaseNode.put("name", testCase.name);
            testCaseNode.put("fullname", testCase.fullname);
            testCaseNode.put("methodname", testCase.methodname);
            testCaseNode.put("classname", testCase.classname);
            testCaseNode.put("result", testCase.result);
            testCaseNode.put("startTime", testCase.startTime.toString());
            testCaseNode.put("endTime", testCase.endTime.toString());
            testCaseNode.put("duration", testCase.duration);
        }

        rootNode.put("runId", UUID.randomUUID().toString());

        String jsonPayload = objectMapper.writeValueAsString(rootNode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to send test results. Status code: " + response.statusCode());
        }
    }

    protected HttpClient createHttpClient(){
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    };

    private static class TestCaseInfo {
        String id;
        String name;
        String fullname;
        String methodname;
        String classname;
        String result;
        Instant startTime;
        Instant endTime;
        long duration;

        TestCaseInfo(Description description, Instant startTime) {
            this(description, startTime, "Unknown");
        }

        TestCaseInfo(Description description, Instant startTime, String result) {
            this.id = UUID.randomUUID().toString();
            this.name = description.getDisplayName();
            this.fullname = description.getTestClass().getName() + "." + description.getMethodName();
            this.methodname = description.getMethodName();
            this.classname = description.getTestClass().getName();
            this.result = result;
            this.startTime = startTime;
            this.endTime = startTime;
            this.duration = 0;
        }
    }
}