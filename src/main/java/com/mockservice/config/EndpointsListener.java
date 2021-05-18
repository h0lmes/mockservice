package com.mockservice.config;

import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class EndpointsListener implements ApplicationListener<ContextRefreshedEvent>, RegisteredRoutesHolder {

    private static final Logger log = LoggerFactory.getLogger(EndpointsListener.class);

    private List<Route> registeredRoutes = new ArrayList<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registeredRoutes.clear();
        ApplicationContext applicationContext = event.getApplicationContext();
        applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class)
                .getHandlerMethods()
                .forEach((mappingInfo, handlerMethod) -> registerRoute(applicationContext, mappingInfo, handlerMethod));
    }

    private void registerRoute(ApplicationContext applicationContext, RequestMappingInfo mappingInfo, HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();
        if (RegistrableController.class.isAssignableFrom(beanType) && mappingInfo.getPatternsCondition() != null) {
            String group = beanType.getSimpleName();
            String beanName = (String) handlerMethod.getBean();
            RegistrableController controller = (RegistrableController) applicationContext.getBean(beanName, beanType);
            if (controller != null) {
                mappingInfo.getPatternsCondition().getPatterns().forEach(path ->
                    mappingInfo.getMethodsCondition().getMethods().forEach(method ->
                        registeredRoutes.add(
                                new Route().setType(controller.getRouteType()).setMethod(method).setPath(path).setGroup(group)
                        )
                    )
                );
            }
        }
    }

    @Override
    public Optional<Route> getRegisteredRoute(RouteType type, RequestMethod method, String path, String suffix) {
        return registeredRoutes.stream()
                .filter(route -> type.equals(route.getType())
                        && method.equals(route.getMethod())
                        && path.equals(route.getPath())
                        && suffix.equals(route.getSuffix()))
                .findFirst();
    }
}
