package com.mockservice.service;

import org.springframework.http.HttpHeaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class ResourceWrapper {

    private static final String HTTP_PREFIX = "HTTP/1.1";
    private static final String HTTP_HEADER_DELIMITER = ": ";

    private int code = 200;
    private HttpHeaders headers = new HttpHeaders();
    private StringBuilder body = new StringBuilder();

    public ResourceWrapper(String resource) {
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
                    line = reader.readLine();
                    continue;
                }

                if (line.trim().isEmpty()) {
                    readingHeaders = false;
                    line = reader.readLine();
                    continue;
                }

                if (readingHeaders) {
                    processHeader(line);
                } else {
                    processLine(line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) { }
    }

    private void processHttpCode(String line) {
        String code = line.substring(HTTP_PREFIX.length() + 1).trim();
        this.code = Integer.valueOf(code);
    }

    private void processHeader(String line) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        int len = HTTP_HEADER_DELIMITER.length();
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + len);
        headers.add(key, value);
    }

    private void processLine(String line) {
        body.append(line).append('\n');
    }

    public String getBody() {
        return body.toString();
    }

    public int getCode() {
        return code;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }
}
