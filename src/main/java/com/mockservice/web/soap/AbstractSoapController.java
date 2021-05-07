package com.mockservice.web.soap;

import com.mockservice.service.MockService;
import com.mockservice.util.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

public class AbstractSoapController {

    private static final Logger log = LoggerFactory.getLogger(AbstractSoapController.class);
    private static final String FAULT_DATA_FILE = "soapFault.xml";
    private static final String FAULT_CODE_PLACEHOLDER = "${code}";
    private static final String FAULT_MESSAGE_PLACEHOLDER = "${message}";

    @Autowired
    @Qualifier("soap")
    MockService mockService;
    private final String folder;
    private final String faultXml;

    public AbstractSoapController() {
        folder = this.getClass().getSimpleName();
        try {
            faultXml = ResourceReader.asString(FAULT_DATA_FILE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(folder, null);
    }

    public ResponseEntity<String> mock(Map<String, String> variables) {
        return mockService.mock(folder, variables);
    }

    @ExceptionHandler
    protected ResponseEntity<Object> handleException(Throwable t, WebRequest request) {
        log.error("", t);

        String body = faultXml
                .replace(FAULT_CODE_PLACEHOLDER, t.getClass().getSimpleName())
                .replace(FAULT_MESSAGE_PLACEHOLDER, t.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
