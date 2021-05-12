package com.mockservice.resource;

import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestMockResource implements MockResource {

    private static final String HTTP_PREFIX = "HTTP/1.1 ";
    private static final String HTTP_HEADER_DELIMITER = ":";
    private static final int HTTP_HEADER_DELIMITER_LEN = HTTP_HEADER_DELIMITER.length();

    private int code = 200;
    private final HttpHeaders headers = new HttpHeaders();
    private final StringTemplate body;

    public RestMockResource(TemplateEngine engine, String resource) {
        body = new StringTemplate(engine);
        fromString(resource);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
    }

    @Override
    public String getBody(@Nullable Map<String, String> variables) {
        return body.toString(variables);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    private void fromString(String resource) {
        List<String> lines = toList(resource);
        List<String> payload = new ArrayList<>();
        boolean head = false;

        for (String line : lines) {
            if (line.startsWith(HTTP_PREFIX)) {
                head = true;
                setHttpCode(line);
            } else if (head && line.trim().isEmpty()) {
                head = false;
            } else if (head) {
                addHeader(line);
            } else {
                payload.add(line);
            }
        }

        payload.forEach(body::add);
    }

    private List<String> toList(String resource) {
        try (BufferedReader reader = new BufferedReader(new StringReader(resource))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setHttpCode(String line) {
        try {
            this.code = Integer.parseInt(line.substring(HTTP_PREFIX.length()).trim());
        } catch (NumberFormatException e) {
            // ignore NaN
        }
    }

    private void addHeader(String line) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + HTTP_HEADER_DELIMITER_LEN).trim();
        headers.add(key, value);
    }
}
