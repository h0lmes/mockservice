package com.mockservice.web.rest;

import com.mockservice.model.Entity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo/api")
public class DemoServiceController extends MockController {

    @GetMapping("entity/filter")
    public ResponseEntity<String> getUntypedResult() {
        return mock();
    }

    @GetMapping("entity/{id}")
    public ResponseEntity<Entity> getTypedEntity() throws Exception {
        return mock(Entity.class);
    }

    @GetMapping("entity")
    public ResponseEntity<String> getUntypedListOfEntities() {
        return mock();
    }

    @PostMapping("entity")
    public ResponseEntity<Entity> postTypedEntity() throws Exception {
        return mock(Entity.class);
    }
}
