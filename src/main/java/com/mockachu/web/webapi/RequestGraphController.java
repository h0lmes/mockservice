package com.mockachu.web.webapi;

import com.mockachu.model.ErrorInfo;
import com.mockachu.service.RequestGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("__webapi__/request-graph")
@CrossOrigin(origins = "*")
public class RequestGraphController {
    private static final Logger log = LoggerFactory.getLogger(RequestGraphController.class);

    private final RequestGraphService service;

    public RequestGraphController(RequestGraphService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String getGraph() {
        return service.getGraph();
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
