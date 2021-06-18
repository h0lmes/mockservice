package com.mockservice.web.webapp;

import com.mockservice.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("web-api")
@CrossOrigin(origins = "*")
public class WebApiLogsController {

    private static final Logger log = LoggerFactory.getLogger(WebApiLogsController.class);

    @GetMapping("log")
    public String getLog() throws IOException {
        return IOUtil.asString(new File("./logs/mockservice.log"));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
