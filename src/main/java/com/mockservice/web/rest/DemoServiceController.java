package com.mockservice.web.rest;

import com.mockservice.service.MockService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("demo/api")
public class DemoServiceController {

    private final MockService mockService;

    public DemoServiceController(MockService mockService) {
        this.mockService = mockService;
    }

    @RequestMapping("entity")
    public String demoApiEntityList(HttpServletRequest request) {
        return mockService.mock(this, request);
    }

    @RequestMapping("entity/{id}")
    public String demoApiEntity(HttpServletRequest request,
                                @PathVariable Map<String, String> pathVariables) {
        return mockService.mock(this, request, pathVariables);
    }

    @RequestMapping("entity/filter")
    public String demoApiEntityListWithQueryParams(HttpServletRequest request,
                                               @RequestParam Map<String,String> requestParams) {
        return mockService.mock(this, request, requestParams);
    }
}
