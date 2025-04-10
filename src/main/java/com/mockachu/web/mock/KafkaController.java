package com.mockachu.web.mock;

import com.mockachu.model.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("__kafka__/v1")
@CrossOrigin(origins = "*")
public class KafkaController {
    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String put(@RequestBody(required = false) String body) {
        log.info(body);
        var parts = body.split(";");
        if (parts.length > 0) log.info("Topic: {}", parts[0]);
        if (parts.length > 1) log.info("Partition: {}", parts[1]);
        if (parts.length > 2) log.info("Time: {}", parts[2]);
        if (parts.length > 3) log.info("Key: {}", new String(Base64.getDecoder().decode(parts[3]), StandardCharsets.UTF_8) );
        if (parts.length > 4) log.info("Value: {}", new String(Base64.getDecoder().decode(parts[4]), StandardCharsets.UTF_8) );
        return "offset=0";
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
