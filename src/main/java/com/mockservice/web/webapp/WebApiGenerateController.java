package com.mockservice.web.webapp;

import com.mockservice.quantum.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("web-api/generate")
@CrossOrigin(origins = "*")
public class WebApiGenerateController {

    private static final Logger log = LoggerFactory.getLogger(WebApiGenerateController.class);

    @GetMapping("json")
    public String json(@RequestParam(name = "root", required = false) String rootValueType) {
        if (rootValueType == null || rootValueType.isEmpty()) {
            return JsonGenerator.generate();
        }

        return JsonGenerator.generate(JsonGenerator.JsonValueType.valueOf(rootValueType));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
