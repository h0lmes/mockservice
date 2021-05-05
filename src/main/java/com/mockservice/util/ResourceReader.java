package com.mockservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ResourceReader {

    private static final Logger log = LoggerFactory.getLogger(ResourceReader.class);

    private static final String JSON_EXTENSION = ".json";
    private static final String XML_EXTENSION = ".xml";
    private static final Map<String, String> files = findFiles();

    private ResourceReader() {
        // hidden
    }

    public static String asString(String path) throws IOException {
        return asString(new DefaultResourceLoader(), path);
    }

    public static String asStringOrFind(ResourceLoader resourceLoader, String path) throws IOException {
        try {
            return asString(resourceLoader, path);
        } catch (FileNotFoundException e) {
            String pathListed = files.get(path.toLowerCase());
            log.warn("File not found: {}", path);
            log.info("Lookup in list: {}", pathListed);
            if (pathListed == null) {
                throw new IOException("File not found in list: " + path, e);
            }
            try {
                return asString(resourceLoader, "classpath:" + pathListed);
            } catch (IOException ex) {
                throw new IOException("Error loading listed file: " + pathListed, ex);
            }
        } catch (IOException e) {
            throw new IOException("Error loading file: " + path, e);
        }
    }

    private static String asString(ResourceLoader resourceLoader, String path) throws IOException {
        return asString(resourceLoader.getResource(path));
    }

    private static String asString(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    private static Map<String, String> findFiles() {
        Map<String, String> result = new HashMap<>();
        find(s -> {
            result.put(s.toLowerCase(), s);
            return true;
        }, JSON_EXTENSION);
        find(s -> {
            result.put(s.toLowerCase(), s);
            return true;
        }, XML_EXTENSION);
        return result;
    }

    private static void find(Function<String, Boolean> visitor, String extension) {
        String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));

        String javaHome = System.getProperty("java.home");
        File file = new File(javaHome + File.separator + "lib");
        if (file.exists()) {
            find(file, file, visitor, extension);
        }

        for (String path : paths) {
            file = new File(path);
            if (file.exists()) {
                find(file, file, visitor, extension);
            }
        }
    }

    private static boolean find(File root, File file, Function<String, Boolean> visitor, String extension) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (!find(root, child, visitor, extension)) {
                        return false;
                    }
                }
            }
        } else {
            if (file.getName().toLowerCase().endsWith(extension)) {
                if (!visitor.apply(createFileName(root, file))) {
                    return false;
                }
            }
        }

        return true;
    }

    private static String createFileName(File root, File file) {
        StringBuilder sb = new StringBuilder();
        String fileName = file.getName();
        sb.append(fileName);
        file = file.getParentFile();
        while (file != null && !file.equals(root)) {
            sb.insert(0, File.separator).insert(0, file.getName());
            file = file.getParentFile();
        }
        return sb.toString();
    }
}
