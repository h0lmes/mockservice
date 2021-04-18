package com.mockservice.web.rest;

import com.mockservice.model.Entity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("demo/api")
public class DemoServiceController extends MockController {

    @GetMapping("entity")
    public ResponseEntity<List<Entity>> demoApiGetListOfEntities() throws Exception {
        return mockList(Entity.class);
    }

    @PostMapping("entity")
    public ResponseEntity<Entity> demoApiPostEntity(@RequestBody Entity entity) throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("name", entity.getName());
        return mock(variables, Entity.class);
    }

    @RequestMapping("entity/{id}")
    public ResponseEntity<Entity> demoApiGetEntity() throws Exception {
        return mock(Entity.class);
    }

    @RequestMapping("entity/filter")
    public ResponseEntity<String> demoApiGetListOfEntitiesWithQueryParams() {
        return mock();
    }
}
