package com.mockachu.web.mock;

import com.mockachu.model.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("__kafka__")
@CrossOrigin(origins = "*")
public class KafkaController {
    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String put(@RequestBody(required = false) String body) {
        log.info(body);
        return "offset=0";
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
