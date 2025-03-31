package com.mockservice.web.webapp;

import com.mockservice.service.VariablesService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("web-api/variables")
@CrossOrigin(origins = "*")
public class WebApiVariablesController {
    private static final Logger log = LoggerFactory.getLogger(WebApiVariablesController.class);

    private final VariablesService variablesService;

    public WebApiVariablesController(VariablesService variablesService) {
        this.variablesService = variablesService;
    }

    @ApiOperation(value = "Get global variables as text", tags = "variables")
    @GetMapping
    public String get() {
        return variablesService.toString();
    }

    @ApiOperation(value = "Set global variables from provided text", tags = "variables")
    @PostMapping
    public void set(@RequestBody(required = false) String body) {
        variablesService.fromString(body);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
