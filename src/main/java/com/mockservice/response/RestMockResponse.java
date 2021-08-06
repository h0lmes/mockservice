package com.mockservice.response;

import com.mockservice.util.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;

public class RestMockResponse extends BaseMockResponse {

    private static final String HTTP_1_1 = "HTTP/1.1";
    private static final String DELIMITER_STRING = "---";
    private static final String HTTP_HEADER_DELIMITER = ":";
    private static final int HTTP_HEADER_DELIMITER_LEN = HTTP_HEADER_DELIMITER.length();

    public RestMockResponse(String response) {
        super();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        read(response);
    }

    private void read(String response) {
        List<String> lines = IOUtils.toList(response);
        boolean readingRequest = false;
        boolean readingHead = false;

        for (String line : lines) {
            if (isResponseStart(line)) {
                readingRequest = false;
                readingHead = true;
            } else if (isRequestStart(line)) {
                readingRequest = true;
                readingHead = true;
                setRequest(line);
            } else if (readingHead) {
                if (line.trim().isEmpty()) {
                    readingHead = false;
                } else if (readingRequest) {
                    addHeader(line, requestHeaders);
                } else {
                    addHeader(line, responseHeaders);
                }
            } else if (!skipLine(line)) {
                if (readingRequest) {
                    requestBody.add(line);
                } else {
                    responseBody.add(line);
                }
            }
        }
    }

    private boolean isRequestStart(String line) {
        return line.trim().endsWith(HTTP_1_1);
    }

    private boolean isResponseStart(String line) {
        return line.trim().startsWith(HTTP_1_1);
    }

    private boolean skipLine(String line) {
        return line.isEmpty() || line.startsWith(DELIMITER_STRING);
    }

    private void setRequest(String line) {
        try {
            String methodAndUrl = line.substring(0, line.indexOf(HTTP_1_1)).trim();
            String methodStr = methodAndUrl.substring(0, methodAndUrl.indexOf(' '));
            requestUrl.add(methodAndUrl.substring(methodAndUrl.indexOf(' ')).trim());
            requestMethod = HttpMethod.valueOf(methodStr);
            containsRequest = true;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid request first line.", e);
        }
    }

    private void addHeader(String line, HttpHeaders toHeaders) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + HTTP_HEADER_DELIMITER_LEN).trim();
        toHeaders.add(key, value);
    }
}
