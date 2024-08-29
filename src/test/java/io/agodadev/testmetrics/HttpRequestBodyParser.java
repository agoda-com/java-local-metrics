package io.agodadev.testmetrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpRequest;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.io.ByteArrayInputStream;

public class HttpRequestBodyParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode parseBody(HttpRequest request) throws Exception {
        if (request.bodyPublisher().isEmpty()) {
            return objectMapper.createObjectNode();
        }

        byte[] bodyBytes = collectBytes(request.bodyPublisher().get());
        try (InputStream inputStream = new ByteArrayInputStream(bodyBytes)) {
            return objectMapper.readTree(inputStream);
        }
    }

    private static byte[] collectBytes(HttpRequest.BodyPublisher bodyPublisher) throws InterruptedException {
        BodySubscriber<byte[]> subscriber = new BodySubscriber<>();
        bodyPublisher.subscribe(subscriber);
        return subscriber.getBody();
    }

    private static class BodySubscriber<T> implements Flow.Subscriber<ByteBuffer> {
        private final CompletableFuture<byte[]> result = new CompletableFuture<>();
        private Flow.Subscription subscription;
        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            byte[] bytes = new byte[item.remaining()];
            item.get(bytes);
            baos.write(bytes, 0, bytes.length);
        }

        @Override
        public void onError(Throwable throwable) {
            result.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            result.complete(baos.toByteArray());
        }

        public byte[] getBody() throws InterruptedException {
            try {
                return result.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}