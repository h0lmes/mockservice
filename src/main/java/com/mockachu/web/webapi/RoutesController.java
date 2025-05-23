package com.mockachu.web.webapi;

import com.mockachu.model.ErrorInfo;
import com.mockachu.model.RouteDto;
import com.mockachu.service.RouteService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("__webapi__/routes")
@CrossOrigin(origins = "*")
public class RoutesController {
    private static final Logger log = LoggerFactory.getLogger(RoutesController.class);

    private final RouteService routeService;

    public RoutesController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RouteDto> getRoutes() {
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Create new route or update existing one", tags = "routes")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RouteDto> patchRoute(@RequestBody List<RouteDto> routes) throws IOException {
        routeService.putRoute(routes.get(0), routes.get(1));
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Create routes skipping existing ones", tags = "routes")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RouteDto> postRoutes(@RequestBody List<RouteDto> routes) throws IOException {
        routeService.putRoutes(routes, false);
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Create routes and update existing ones", tags = "routes")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RouteDto> putRoutes(@RequestBody List<RouteDto> routes) throws IOException {
        routeService.putRoutes(routes, true);
        return routeService.getRoutes();
    }

    @ApiOperation(value = "Delete routes", tags = "routes")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RouteDto> deleteRoutes(@RequestBody List<RouteDto> routes) throws IOException {
        routeService.deleteRoutes(routes);
        return routeService.getRoutes();
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
