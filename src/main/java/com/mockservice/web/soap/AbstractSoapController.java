package com.mockservice.web.soap;

import com.mockservice.service.MockService;
import com.mockservice.template.StringTemplate;
import com.mockservice.util.ResourceReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

public class AbstractSoapController {

    @Autowired
    @Qualifier("soap")
    MockService mockService;
    private final String folder;
    StringTemplate faultTemplate = new StringTemplate(ResourceReader.asString("classpath:/soapFault.xml"));

    public AbstractSoapController() {
        folder = this.getClass().getSimpleName();
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(folder, null);
    }

    public ResponseEntity<String> mock(Map<String, String> variables) {
        return mockService.mock(folder, variables);
    }

    @ExceptionHandler
    protected ResponseEntity<Object> handleException(Throwable t, WebRequest request) {
        Map<String, String> variables = new HashMap<>();
        variables.put("type", t.getClass().getSimpleName());
        variables.put("message", t.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(faultTemplate.toString(variables));
    }
}
