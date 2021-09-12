package com.mockservice.web.webapp;

import com.mockservice.service.route.RouteDto;
import com.mockservice.service.route.RouteService;
import com.mockservice.service.route.RouteVariableDto;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("web-api/routes")
@CrossOrigin(origins = "*")
public class WebApiRoutesController {

    private static final Logger log = LoggerFactory.getLogger(WebApiRoutesController.class);

    private final RouteService routeService;

    public WebApiRoutesController(RouteService routeService) {
        this.routeService = routeService;
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(new ErrorInfo(e));
    }

    //-----------------------------------------------------------------------------------

    @GetMapping
    public List<RouteDto> routes() {
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
}
