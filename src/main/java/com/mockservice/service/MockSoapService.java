package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.request.RequestFacade;
import com.mockservice.request.SoapRequestFacade;
import com.mockservice.resource.MockResource;
import com.mockservice.resource.SoapMockResource;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.FileReaderWriterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service("soap")
public class MockSoapService implements MockService {

    private static final Logger log = LoggerFactory.getLogger(MockSoapService.class);

    private static final String FAULT_CODE_PLACEHOLDER = "${code}";
    private static final String FAULT_MESSAGE_PLACEHOLDER = "${message}";

    private final HttpServletRequest request;
    private final TemplateEngine templateEngine;
    private final RouteService routeService;
    private final ActiveScenariosService activeScenariosService;
    private final ConfigRepository configRepository;
    private final ConcurrentLruCache<Route, MockResource> resourceCache;
    private String errorBody;

    public MockSoapService(HttpServletRequest request,
                           TemplateEngine templateEngine,
                           RouteService routeService,
                           ActiveScenariosService activeScenariosService,
                           ConfigRepository configRepository,
                           @Value("${application.cache.soap-resource}") int cacheSizeLimit,
                           @Value("${application.soap-error-data-file}") String soapErrorDataFile) {
        this.request = request;
        this.templateEngine = templateEngine;
        this.routeService = routeService;
        this.activeScenariosService = activeScenariosService;
        this.configRepository = configRepository;
        resourceCache = new ConcurrentLruCache<>(cacheSizeLimit, this::loadResource);

        try {
            errorBody = FileReaderWriterUtil.asString(soapErrorDataFile);
        } catch (IOException e) {
            log.error("Error loading SOAP error data file, using fallback.", e);
            errorBody = "<code>" + FAULT_CODE_PLACEHOLDER + "</code>\n<message>" + FAULT_MESSAGE_PLACEHOLDER + "</message>";
        }
    }

    private MockResource loadResource(Route route) {
        return routeService.getEnabledRoute(route)
                .map(r -> new SoapMockResource(templateEngine, r.getResponse()))
                .orElse(null);
    }

    @Override
    public void cacheRemove(Route route) {
        resourceCache.remove(route);
    }

    @Override
    public ResponseEntity<String> mock(Map<String, String> variables) {
        RequestFacade requestFacade = new SoapRequestFacade(request);
        Route route = getRoute(requestFacade);
        log.info("Route requested: {}", route);
        MockResource resource = resourceCache.get(route);
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(resource.getBody(requestVariables));
    }

    private Route getRoute(RequestFacade requestFacade) {
        Route route = new Route()
                .setMethod(requestFacade.getRequestMethod())
                .setPath(requestFacade.getEndpoint());

        boolean random = configRepository.getSettings().getRandomAlt() || configRepository.getSettings().getQuantum();
        String alt = requestFacade
                .getAlt()
                .or(() -> random ? routeService.getRandomAltFor(route) : Optional.empty())
                .or(() -> activeScenariosService.getAltFor(requestFacade.getRequestMethod(), requestFacade.getEndpoint()))
                .orElse("");
        return route.setAlt(alt);
    }

    @Override
    public String mockError(Throwable t) {
        return errorBody
                .replace(FAULT_CODE_PLACEHOLDER, t.getClass().getSimpleName())
                .replace(FAULT_MESSAGE_PLACEHOLDER, t.getMessage());
    }
}
