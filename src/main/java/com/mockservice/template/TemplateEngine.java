package com.mockservice.template;

import java.util.Map;
import java.util.function.Function;

public interface TemplateEngine {
    Map<String, Function<String[], String>> getFunctions();
    boolean isFunction(String arg0);
}
