package com.mockachu.web.webapi;

import com.mockachu.model.ErrorInfo;
import com.mockachu.service.ContextService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("__webapi__/context")
@CrossOrigin(origins = "*")
public class ContextController {
    private static final Logger log = LoggerFactory.getLogger(ContextController.class);

    private final ContextService contextService;

    public ContextController(ContextService contextService) {
        this.contextService = contextService;
    }

    @ApiOperation(value = "Get global variables as text", tags = "context")
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String get() {
        return contextService.getAsString();
    }

    @ApiOperation(value = "Set global variables from provided text", tags = "context")
    @PostMapping
    public void set(@RequestBody(required = false) String body) {
        contextService.setFromString(body);
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
