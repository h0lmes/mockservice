package com.mockservice.service;

import com.mockservice.mockconfig.Route;

import java.io.IOException;
import java.util.List;

public interface ResourceService {
    List<Route> files();
    String load(Route route) throws IOException;
}
