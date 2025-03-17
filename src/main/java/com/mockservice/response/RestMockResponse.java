package com.mockservice.response;

import com.mockservice.util.IOUtils;
import org.springframework.http.HttpHeaders;

import java.util.List;

public class RestMockResponse extends BaseMockResponse {

    private static final String HTTP_1_1 = "HTTP/1.1";
    private static final String DELIMITER_STRING = "---";
    private static final String HTTP_HEADER_DELIMITER = ":";
    private static final int HTTP_HEADER_DELIMITER_LEN = HTTP_HEADER_DELIMITER.length();

    public RestMockResponse(int responseCode, String response) {
        super(responseCode);
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        read(response);
    }

    private void read(String response) {
        List<String> lines = IOUtils.toList(response);
        boolean readingHead = false;

        for (String line : lines) {
            if (isResponseStart(line)) {
                readingHead = true;
            } else if (readingHead) {
                if (line.trim().isEmpty()) {
                    readingHead = false;
                } else {
                    addHeader(line, responseHeaders);
                }
            } else if (!skipLine(line)) {
                responseBody.add(line);
            }
        }
    }

    private boolean isResponseStart(String line) {
        return line.trim().startsWith(HTTP_1_1);
    }

    private boolean skipLine(String line) {
        return line.isEmpty() || line.startsWith(DELIMITER_STRING);
    }

    private void addHeader(String line, HttpHeaders toHeaders) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + HTTP_HEADER_DELIMITER_LEN).trim();
        toHeaders.add(key, value);
    }
}
