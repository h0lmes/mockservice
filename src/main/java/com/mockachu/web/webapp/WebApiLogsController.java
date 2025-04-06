package com.mockachu.web.webapp;

import com.mockachu.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("web-api/log")
@CrossOrigin(origins = "*")
public class WebApiLogsController {

    private static final Logger log = LoggerFactory.getLogger(WebApiLogsController.class);

    @GetMapping
    public String getLog() throws IOException {
        return IOUtils.asString(new File("./logs/mockachu.log"));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
