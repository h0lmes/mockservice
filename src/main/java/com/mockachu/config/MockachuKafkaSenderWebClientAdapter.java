package com.mockachu.config;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.CompletableFuture;

public class MockachuKafkaSenderWebClientAdapter implements MockachuKafkaSender {
    private static final Logger log = LoggerFactory.getLogger(
            MockachuKafkaSenderWebClientAdapter.class);

    private static final String KAFKA_ENDPOINT = "__kafka__/v1";
    private final WebClient client;
    private final String baseUri;

    public MockachuKafkaSenderWebClientAdapter(String baseUri) {
        this.baseUri = baseUri.endsWith("/") ?
                baseUri + KAFKA_ENDPOINT : baseUri + "/" + KAFKA_ENDPOINT;
        var httpClient = HttpClient.create();
        var clientHttpConnector = new ReactorClientHttpConnector(httpClient);
        this.client = WebClient.builder().clientConnector(clientHttpConnector).build();
    }

    public CompletableFuture<RecordMetadata> send(String uri,
                                                  String message,
                                                  String topic,
                                                  int partition) {
        log.info("send({}, {}, {}, {})", uri, message, topic, partition);
        var future = new CompletableFuture<RecordMetadata>();
        client.post().uri(baseUri + uri).bodyValue(message)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        success -> complete(future, success, topic, partition),
                        future::completeExceptionally
                );
        return future;
    }

    private void complete(CompletableFuture<RecordMetadata> future,
                          String result,
                          String topic,
                          int partition) {
        log.info("complete({}, {}, {})", result, topic, partition);
        var offset = getValueLong(result, "offset", 0);

        var topicPartition = new TopicPartition(topic, partition);
        var metadata = new RecordMetadata(
                topicPartition, offset, 0, 0, 0, 0);
        future.complete(metadata);
    }

    private static String getValue(String string, String key) {
        int start = string.indexOf(key + "=");
        if (start < 0) {
            return "";
        }
        start += key.length();
        int end = string.indexOf(";", start);
        if (end <= start) {
            return string.substring(start + 1);
        }
        return string.substring(start + 1, end);
    }

    private static long getValueLong(String string, String key, long defVal) {
        var str = getValue(string, key);
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return defVal;
        }
    }

    private static int getValueInt(String string, String key, int defVal) {
        var str = getValue(string, key);
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return defVal;
        }
    }

    private static void main(String[] args) {
        String str = "test=1;git=2;hit=3";
        assertTrue("1".equals(MockachuKafkaSenderWebClientAdapter.getValue(str, "test")), "1.1");
        assertTrue("2".equals(MockachuKafkaSenderWebClientAdapter.getValue(str, "git")), "1.2");
        assertTrue("3".equals(MockachuKafkaSenderWebClientAdapter.getValue(str, "hit")), "1.3");
        String str2 = "test=1";
        assertTrue("1".equals(MockachuKafkaSenderWebClientAdapter.getValue(str2, "test")), "2");
        String str3 = "test=";
        assertTrue("".equals(MockachuKafkaSenderWebClientAdapter.getValue(str3, "test")), "3");
        String str4 = "test=;git=2";
        assertTrue("".equals(MockachuKafkaSenderWebClientAdapter.getValue(str4, "test")), "4");
        String str5 = "test";
        assertTrue("".equals(MockachuKafkaSenderWebClientAdapter.getValue(str5, "test")), "5");
        String str6 = "tes";
        assertTrue("".equals(MockachuKafkaSenderWebClientAdapter.getValue(str6, "test")), "6");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) throw new RuntimeException("Test failed: " + message);
    }
}
