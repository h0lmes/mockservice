package com.mockservice.service;

import java.io.IOException;
import java.util.List;

public interface ResourceService {
    List<String> files();
    String load(String path) throws IOException;
}
