package com.mockservice.config;

import com.mockservice.mockconfig.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
public class EndpointsListener implements ApplicationListener<ContextRefreshedEvent>, RegisteredRoutesHolder {

    private static final Logger log = LoggerFactory.getLogger(EndpointsListener.class);

    private List<Route> registeredRoutes = new ArrayList<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registeredRoutes.clear();
        ApplicationContext applicationContext = event.getApplicationContext();
        RequestMappingHandlerMapping mapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        map.forEach((key, value) -> {
            Class<?> beanType = value.getBeanType();
            if (RegistrableController.class.isAssignableFrom(beanType) && key.getPatternsCondition() != null) {
                String group = beanType.getSimpleName();
                String beanName = (String) value.getBean();
                RegistrableController controller = (RegistrableController) applicationContext.getBean(beanName, beanType);
                if (controller != null) {
                    key.getPatternsCondition().getPatterns().forEach(path ->
                        key.getMethodsCondition().getMethods().forEach(method ->
                            registeredRoutes.add(new Route(controller.getRouteType(), method, path, group))
                        )
                    );
                }
            }
        });
    }

    @Override
    public Optional<Route> getRegisteredRoute(Route lookFor) {
        return registeredRoutes.stream()
                .filter(route -> route.equals(lookFor))
                .findFirst();
    }
}
