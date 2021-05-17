package com.mockservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.mockservice.mockconfig.Config;
import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class YamlConfigService implements ConfigService {

    @Value("${application.config-path}")
    private String configPath = "classpath:data/config.yml";
    private Config config;

    public YamlConfigService() {
        try {
            readConfigFromResource();
            String path = new File(".\\config.yml").getCanonicalPath();
            saveConfigToFile(path);
        } catch (IOException e) {
            config = new Config();
        }
    }

    private void readConfigFromResource() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(configPath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            config = mapper.readValue(reader, Config.class);
        }
    }

    private void readConfigFromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        config = mapper.readValue(new File(path), Config.class);
    }

    private void saveConfigToFile(String path) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        factory.enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
        factory.disable(YAMLGenerator.Feature.INDENT_ARRAYS);
        factory.disable(YAMLGenerator.Feature.SPLIT_LINES);
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.writeValue(new File(path), config);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Stream<Route> getActiveRoutes() {
        return getConfig().getGroups().stream()
                .flatMap(
                        group -> group.getRoutes().stream()
                                .filter(route -> !route.getDisabled())
                                .peek(route -> route.setGroup(group.getName()))
                );
    }

    @Override
    public Optional<Route> getActiveRoute(RouteType type, RequestMethod method, String path) {
        return getActiveRoutes()
                .filter(route -> type.equals(route.getType()) && method.equals(route.getMethod()) && path.equals(route.getPath()))
                .findFirst();
    }

    @Override
    public Optional<Route> getActiveRoute(Route lookFor) {
        return getActiveRoutes()
                .filter(route -> route.equals(lookFor))
                .findFirst();
    }
}
