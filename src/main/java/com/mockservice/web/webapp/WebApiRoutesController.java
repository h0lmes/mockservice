package com.mockservice.web.webapp;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
import com.mockservice.service.RouteService;
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
    public List<Route> routes() {
        return routeService.getRoutesAsList();
    }

    @PutMapping
    public List<Route> putRoute(@RequestBody List<Route> routes) throws IOException, RouteAlreadyExistsException {
        if (routes.size() != 2) {
            throw new IllegalArgumentException("There must be exactly 2 routes, the old and the new one.");
        }
        return routeService.putRoute(routes.get(0), routes.get(1));
    }

    @DeleteMapping
    public List<Route> deleteRoute(@RequestBody Route route) throws IOException {
        return routeService.deleteRoute(route);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleRouteAlreadyExistsException(RouteAlreadyExistsException e) {
        log.warn(e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
