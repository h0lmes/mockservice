package com.mockservice.service;

import com.mockservice.template.StringTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.Supplier;

public class MockResource {

    private static final String HTTP_PREFIX = "HTTP/1.1 ";
    private static final int HTTP_PREFIX_LEN = HTTP_PREFIX.length();
    private static final String HTTP_HEADER_DELIMITER = ": ";

    private int code = 200;
    private HttpHeaders headers = new HttpHeaders();
    private StringTemplate body = new StringTemplate();

    public MockResource(String resource) {
        fromString(resource);
    }

    private void fromString(String resource) {
        try (BufferedReader reader = new BufferedReader(new StringReader(resource))) {
            String line = reader.readLine();
            boolean readingHeaders = false;

            while (line != null) {
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
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void processHttpCode(String line) {
        try {
            this.code = Integer.parseInt(line.substring(HTTP_PREFIX_LEN).trim());
        } catch (NumberFormatException e) {
            // do nothing
        }
    }

    private void processHeader(String line) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        int len = HTTP_HEADER_DELIMITER.length();
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + len);
        headers.add(key, value);
    }

    private void processLine(String line) {
        body.add(line);
    }

    public String getBody(@Nullable Map<String, String> variables, @Nullable Map<String, Supplier<String>> suppliers) {
        return body.toString(variables, suppliers);
    }

    public int getCode() {
        return code;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }
}
