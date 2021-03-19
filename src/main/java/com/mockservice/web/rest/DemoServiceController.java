package com.mockservice.web.rest;

import com.mockservice.model.Entity;
import com.mockservice.service.MockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("demo/api")
public class DemoServiceController {

    private final MockService mockService;

    public DemoServiceController(MockService mockService) {
        this.mockService = mockService;
    }

    @GetMapping("entity")
    public ResponseEntity<List<Entity>> demoApiGetListOfEntities(HttpServletRequest request) throws Exception {
        return mockService.mockList(this, request, Entity.class);
    }

    @PostMapping("entity")
    public ResponseEntity<Entity> demoApiPostEntity(HttpServletRequest request,
                                                      @RequestBody Entity entity) throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("name", entity.getName());
        return mockService.mock(this, request, variables, Entity.class);
    }

    @RequestMapping("entity/{id}")
    public ResponseEntity<Entity> demoApiGetEntity(HttpServletRequest request,
                                                @PathVariable Map<String, String> pathVariables) throws Exception {
        return mockService.mock(this, request, pathVariables, Entity.class);
    }

    @RequestMapping("entity/filter")
    public ResponseEntity<String> demoApiGetListOfEntitiesWithQueryParams(HttpServletRequest request,
                                                                     @RequestParam Map<String,String> requestParams) {
        return mockService.mock(this, request, requestParams);
    }
}
