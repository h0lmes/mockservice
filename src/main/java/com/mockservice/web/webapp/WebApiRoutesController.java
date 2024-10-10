package com.mockservice.web.webapp;

import com.mockservice.model.RouteDto;
import com.mockservice.model.RouteVariableDto;
import com.mockservice.service.RouteService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("web-api/routes")
@CrossOrigin(origins = "*")
public class WebApiRoutesController {

    private static final Logger log = LoggerFactory.getLogger(WebApiRoutesController.class);

    private final RouteService routeService;

    public WebApiRoutesController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<RouteDto> getRoutes() {
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Create new route or update existing one", tags = "routes")
    @PatchMapping
    public List<RouteDto> patchRoute(@RequestBody List<RouteDto> routes) throws IOException {
        routeService.putRoute(routes.get(0), routes.get(1));
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Create routes skipping existing ones", tags = "routes")
    @PostMapping
    public List<RouteDto> postRoutes(@RequestBody List<RouteDto> routes) throws IOException {
        routeService.putRoutes(routes, false);
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Create routes and update existing ones", tags = "routes")
    @PutMapping
    public List<RouteDto> putRoutes(@RequestBody List<RouteDto> routes) throws IOException {
        routeService.putRoutes(routes, true);
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Delete routes", tags = "routes")
    @DeleteMapping
    public List<RouteDto> deleteRoutes(@RequestBody List<RouteDto> routes) throws IOException {
        routeService.deleteRoutes(routes);
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Set specific variable for the specified route", tags = "routes")
    @PutMapping("variables")
    public RouteVariableDto setVariable(@RequestBody RouteVariableDto variable) {
        return routeService.setRouteVariable(variable);
    }

    @ApiOperation(value = "Clear specific variable for the specified route", tags = "routes")
    @DeleteMapping("variables")
    public RouteVariableDto clearVariable(@RequestBody RouteVariableDto variable) {
        return routeService.clearRouteVariable(variable);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
