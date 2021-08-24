package com.mockservice.web.webapp;

import com.mockservice.domain.Route;
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
    public List<Route> routes() {
        return routeService.getRoutesAsList();
    }

    @ApiOperation(value = "Creates new route or updates existing", tags = "routes")
    @PatchMapping
    public List<Route> patchRoute(@RequestBody Route route) throws IOException {
        return routeService.putRoute(route);
    }

    @ApiOperation(value = "Creates routes and updates already existing", tags = "routes")
    @PutMapping
    public List<Route> putRoutes(@RequestBody List<Route> routes) throws IOException {
        return routeService.putRoutes(routes, true);
    }

    @ApiOperation(value = "Creates routes skipping already existing", tags = "routes")
    @PostMapping
    public List<Route> postRoutes(@RequestBody List<Route> routes) throws IOException {
        return routeService.putRoutes(routes, false);
    }

    @ApiOperation(value = "Deletes listed routes", tags = "routes")
    @DeleteMapping
    public List<Route> deleteRoutes(@RequestBody List<Route> routes) throws IOException {
        return routeService.deleteRoutes(routes);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
