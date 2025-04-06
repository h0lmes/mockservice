package com.mockachu.web.webapp;

import com.mockachu.service.ContextService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("web-api/context")
@CrossOrigin(origins = "*")
public class WebApiVariablesController {
    private static final Logger log = LoggerFactory.getLogger(WebApiVariablesController.class);

    private final ContextService contextService;

    public WebApiVariablesController(ContextService contextService) {
        this.contextService = contextService;
    }

    @ApiOperation(value = "Get global variables as text", tags = "context")
    @GetMapping
    public String get() {
        return contextService.getAsString();
    }

    @ApiOperation(value = "Set global variables from provided text", tags = "context")
    @PostMapping
    public void set(@RequestBody(required = false) String body) {
        contextService.setFromString(body);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
