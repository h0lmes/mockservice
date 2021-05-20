package com.mockservice.web.webapp;

import com.mockservice.mockconfig.Route;
import com.mockservice.service.ResourceService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("web-api")
@CrossOrigin(origins = "*")
public class WebApiController {

    private final ResourceService resourceService;

    public WebApiController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("datafiles")
    public List<Route> dataFiles() {
        return resourceService.files();
    }
}
