package com.mockservice.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class DemoServiceController extends AbstractRestController {

    @GetMapping("entity/filter")
    public ResponseEntity<String> getEntityWithParameters() {
        return mock();
    }

    @GetMapping("entity/{id}")
    public ResponseEntity<String> getEntityById() {
        return mock();
    }

    @GetMapping("entity")
    public ResponseEntity<String> listEntity() {
        return mock();
    }

    @PostMapping("entity")
    public ResponseEntity<String> postEntity() {
        return mock();
    }
}
