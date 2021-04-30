package com.mockservice.web.soap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SoapController extends AbstractSoapController {

    @PostMapping("mockNumberConversion")
    public ResponseEntity<String> mockNumberConversion() {
        return mock();
    }
}
