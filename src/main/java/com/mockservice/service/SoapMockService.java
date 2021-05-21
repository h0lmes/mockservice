package com.mockservice.service;

import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import com.mockservice.request.RequestFacade;
import com.mockservice.request.SoapRequestFacade;
import com.mockservice.resource.MockResource;
import com.mockservice.resource.SoapMockResource;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Service("soap")
public class SoapMockService implements MockService {

    private static final Logger log = LoggerFactory.getLogger(SoapMockService.class);

    private static final String FAULT_CODE_PLACEHOLDER = "${code}";
    private static final String FAULT_MESSAGE_PLACEHOLDER = "${message}";

    private final HttpServletRequest request;
    private final ResourceService resourceService;
    private final TemplateEngine templateEngine;
    private final ConfigService configService;
    private final ConcurrentLruCache<Route, MockResource> resourceCache;
    private String errorBody;

    public SoapMockService(HttpServletRequest request,
                           ResourceService resourceService,
                           TemplateEngine templateEngine,
                           ConfigService configService,
                           @Value("${application.cache.soap-resource}") int cacheSizeLimit,
                           @Value("${application.soap-error-data-file}") String soapErrorDataFile) {
        this.request = request;
        this.resourceService = resourceService;
        this.templateEngine = templateEngine;
        this.configService = configService;
        resourceCache = new ConcurrentLruCache<>(cacheSizeLimit, this::loadResource);

        try {
            errorBody = ResourceReader.asString(soapErrorDataFile);
        } catch (IOException e) {
            log.error("Error loading SOAP error data file, using fallback.", e);
            errorBody = "<code>" + FAULT_CODE_PLACEHOLDER + "</code>\n<message>" + FAULT_MESSAGE_PLACEHOLDER + "</message>";
        }
    }

    private MockResource loadResource(Route route) {
        Route configuredRoute = configService.getEnabledRoute(route).orElse(null);
        try {
            return new SoapMockResource(templateEngine, configuredRoute == null ? resourceService.load(route) : configuredRoute.getResponse());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ResponseEntity<String> mock(Map<String, String> variables) {
        RequestFacade requestFacade = new SoapRequestFacade(request);
        Route route = getRoute(requestFacade);
        log.info("File requested: {}", route);
        MockResource resource = resourceCache.get(route);
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(resource.getBody(requestVariables));
    }

    private Route getRoute(RequestFacade requestFacade) {
        return new Route()
                .setType(RouteType.SOAP)
                .setMethod(requestFacade.getRequestMethod())
                .setPath(requestFacade.getEndpoint())
                .setSuffix(requestFacade.getSuffix());
    }

    @Override
    public String mockError(Throwable t) {
        return errorBody
                .replace(FAULT_CODE_PLACEHOLDER, t.getClass().getSimpleName())
                .replace(FAULT_MESSAGE_PLACEHOLDER, t.getMessage());
    }
}
