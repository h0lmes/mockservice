package com.mockservice.util;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class IOUtils {

    private static final String CLASSPATH = "classpath:";

    private IOUtils() {
        // private
    }

    public static String asString(String path) throws IOException {
        return asString(new DefaultResourceLoader(), path);
    }

    public static String asString(ResourceLoader resourceLoader, String path) throws IOException {
        if (!path.startsWith(CLASSPATH)) {
            path = CLASSPATH + path;
        }
        return asString(resourceLoader.getResource(path));
    }

    private static String asString(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    public static String asString(File file) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    public static List<String> toList(String resource) {
        try (BufferedReader reader = new BufferedReader(new StringReader(resource))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
