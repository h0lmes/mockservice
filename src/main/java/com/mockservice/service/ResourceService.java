package com.mockservice.service;

import com.mockservice.service.model.DataFileInfo;

import java.io.IOException;
import java.util.List;

public interface ResourceService {
    List<DataFileInfo> files();
    String load(String path) throws IOException;
}
