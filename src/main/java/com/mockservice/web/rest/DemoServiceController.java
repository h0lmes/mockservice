package com.mockservice.web.rest;

import com.mockservice.model.Entity;
import com.mockservice.service.MockService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("demo/api")
public class DemoServiceController {

    private final MockService mockService;

    public DemoServiceController(MockService mockService) {
        this.mockService = mockService;
    }

    @RequestMapping("entity")
    public List<Entity> demoApiListOfEntity(HttpServletRequest request) throws Exception {
        return mockService.mockTypedList(this, request, Entity.class);
    }

    @RequestMapping("entity/{id}")
    public Entity demoApiEntity(HttpServletRequest request,
                                @PathVariable Map<String, String> pathVariables) throws Exception {
        return mockService.mockTyped(this, request, pathVariables, Entity.class);
    }

    @RequestMapping("entity/filter")
    public String demoApiListOfEntityWithQueryParams(HttpServletRequest request,
                                               @RequestParam Map<String,String> requestParams) {
        return mockService.mock(this, request, requestParams);
    }
}
