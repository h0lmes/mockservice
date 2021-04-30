package com.mockservice.web.ws;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WsController extends AbstractWsController {

    @PostMapping("mockNumberConversion")
    public ResponseEntity<String> mockNumberConversion() {
        return mock();
    }
}
