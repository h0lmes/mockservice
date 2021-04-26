package com.mockservice.service;

import com.mockservice.template.StringTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.Function;

public class MockResource {

    private static final String HTTP_PREFIX = "HTTP/1.1 ";
    private static final int HTTP_PREFIX_LEN = HTTP_PREFIX.length();
    private static final String HTTP_HEADER_DELIMITER = ": ";
    private static final int HTTP_HEADER_DELIMITER_LEN = HTTP_HEADER_DELIMITER.length();

    private int code = 200;
    private HttpHeaders headers = new HttpHeaders();
    private StringTemplate body = new StringTemplate();
    private boolean readingHeaders = false;

    public MockResource(String resource) {
        fromString(resource);
    }

    private void fromString(String resource) {
        try (BufferedReader reader = new BufferedReader(new StringReader(resource))) {

            reader.lines().forEach(line -> {
                if (line.startsWith(HTTP_PREFIX)) {
                    readingHeaders = true;
                    processHttpCode(line);
                } else if (readingHeaders && line.trim().isEmpty()) {
                    readingHeaders = false;
                } else if (readingHeaders) {
                    processHeader(line);
                } else {
                    processLine(line);
                }
            });

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void processHttpCode(String line) {
        try {
            this.code = Integer.parseInt(line.substring(HTTP_PREFIX_LEN).trim());
        } catch (NumberFormatException e) {
            // ignore NaN
        }
    }

    private void processHeader(String line) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + HTTP_HEADER_DELIMITER_LEN);
        headers.add(key, value);
    }

    private void processLine(String line) {
        body.add(line);
    }

    public String getBody(@Nullable Map<String, String> variables, @Nullable Map<String, Function<String[], String>> functions) {
        return body.toString(variables, functions);
    }

    public int getCode() {
        return code;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }
}
