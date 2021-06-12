package com.mockservice.util;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class IOUtil {

    private static final String CLASSPATH = "classpath:";

    private IOUtil() {
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

    public static void writeFile(File file, String data) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] strToBytes = data.getBytes();
            outputStream.write(strToBytes);
        }
    }

    public static List<String> toList(String resource) {
        try (BufferedReader reader = new BufferedReader(new StringReader(resource))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
