package com.mockservice.service;

import com.mockservice.util.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class DefaultResourceService implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(DefaultResourceService.class);

    private static final String DATA_FOLDER = "data";
    private static final String DATA_FILE_REGEX = ".+" + DATA_FOLDER + "[\\/\\\\](.+\\.json|.+\\.xml)$";

    private final ResourceLoader resourceLoader;
    private final Map<String, String> mockDataFiles;

    public DefaultResourceService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        mockDataFiles = findDataFiles();
    }

    @Override
    public List<String> files() {
        List<String> list = new ArrayList<>();
        mockDataFiles.forEach((k, v) -> list.add(v));
        list.sort(String::compareToIgnoreCase);
        return list;
    }

    @Override
    public String load(String path) throws IOException {
        try {
            return ResourceReader.asString(resourceLoader, DATA_FOLDER + File.separator + path);
        } catch (FileNotFoundException e) {
            String pathListed = mockDataFiles.get(path.toLowerCase());
            log.warn("File not found: {}", path);
            log.info("Lookup in list: {}", pathListed);
            if (pathListed == null) {
                throw new IOException("File not found in list: " + path, e);
            }
            try {
                return ResourceReader.asString(resourceLoader, DATA_FOLDER + File.separator + pathListed);
            } catch (IOException ex) {
                throw new IOException("Error loading listed file: " + pathListed, ex);
            }
        } catch (IOException e) {
            throw new IOException("Error loading file: " + path, e);
        }
    }

    private static Map<String, String> findDataFiles() {
        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile(DATA_FILE_REGEX, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
        try {
            findResourcesMatchingPattern(s -> result.put(s.toLowerCase(), s), pattern);
        } catch (URISyntaxException | IOException e) {
            log.error("", e);
        }
        return result;
    }

    private static void findResourcesMatchingPattern(Consumer<String> consumer, Pattern pattern) throws URISyntaxException, IOException {
        CodeSource src = DefaultResourceService.class.getProtectionDomain().getCodeSource();
        URL url = src.getLocation();
        URI uri = url.toURI();
        if (uri.getScheme().equals("jar")) {
            walkJar(uri, consumer, pattern);
        } else {
            walkPath(Paths.get(uri), consumer, pattern);
        }
    }

    private static void walkJar(URI uri, Consumer<String> consumer, Pattern pattern) throws IOException {
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            for (Path path : fileSystem.getRootDirectories()) {
                walkPath(path, consumer, pattern);
            }
        }
    }

    private static void walkPath(Path path, Consumer<String> consumer, Pattern pattern) throws IOException {
        try (Stream<Path> files = Files.walk(path, 10)) {
            for (Iterator<Path> it = files.iterator(); it.hasNext(); ) {
                String file = it.next().toString();
                Matcher matcher = pattern.matcher(file);
                if (matcher.find()) {
                    consumer.accept(matcher.group(1));
                }
            }
        }
    }
}
