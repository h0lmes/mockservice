package com.mockachu.web.webapp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
    Serve static web app assets through this filter
    to bypass MockController /** mappings.

    IMPORTANT!
    If more pages or new static asset paths added in future
    (e.g. /fonts/, /icons/) - add them here.
*/
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebAppForwardFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(WebAppForwardFilter.class);

    private static final Set<String> WEBAPP_ROUTES = Set.of(
            "/",
            "/request-graph",
            "/context",
            "/kafka",
            "/import",
            "/generate",
            "/settings",
            "/config",
            "/log",
            "/about"
            );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (WEBAPP_ROUTES.contains(uri)) {
            if (log.isDebugEnabled()) {
                log.debug("Serving index.html for SPA route: {}", uri);
            }
            try (var in = getClass().getResourceAsStream("/webapp/index.html")) {
                if (in != null) {
                    response.setContentType("text/html;charset=UTF-8");
                    in.transferTo(response.getOutputStream());
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "index.html not found");
                }
            }
            return;
        }

        if (uri.startsWith("/_nuxt/")) {
            String resourcePath = "/webapp" + uri;
            if (log.isDebugEnabled()) {
                log.debug("Serving static asset: {}", resourcePath);
            }
            try (var in = getClass().getResourceAsStream(resourcePath)) {
                if (in != null) {
                    response.setContentType(resolveContentType(uri));
                    in.transferTo(response.getOutputStream());
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, resourcePath + " not found");
                }
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    String resolveContentType(String uri) {
        if (uri.endsWith(".js"))  return "application/javascript;charset=UTF-8";
        if (uri.endsWith(".css")) return "text/css;charset=UTF-8";
        if (uri.endsWith(".svg")) return "image/svg+xml";
        if (uri.endsWith(".png")) return "image/png";
        if (uri.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
