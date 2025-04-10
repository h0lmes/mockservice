package com.mockachu.web.webapi;

import com.mockachu.model.ErrorInfo;
import com.mockachu.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("__webapi__/log")
@CrossOrigin(origins = "*")
public class LogsController {
    private static final Logger log = LoggerFactory.getLogger(LogsController.class);

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLog() throws IOException {
        return IOUtils.asString(new File("./logs/mockachu.log"));
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
