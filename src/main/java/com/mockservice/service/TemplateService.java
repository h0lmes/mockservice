package com.mockservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Supplier;

@Service
public class TemplateService {

    private static final String VAR_START = "${";
    private static final String VAR_END = "}";

    String resolve(String template, Map<String, String> vars) {
        template = resolveVariables(template, vars);
        template = resolveConstants(template);
        return template;
    }

    private static String resolveConstants(String template) {
        for (TemplateConstants constant : TemplateConstants.values()) {
            template = replaceAll(template, constant.getPlaceholder(), constant.getSupplier());
        }
        return template;
    }

    private static String resolveVariables(String template, Map<String, String> vars) {
        if (vars != null) {
            for (Map.Entry<String, String> e : vars.entrySet()) {
                template = replaceAll(template, VAR_START + e.getKey() + VAR_END, e.getValue());
            }
        }
        return template;
    }

    private static String replaceAll(String template, String k, Supplier<String> s) {
        int start = template.indexOf(k);
        int len = k.length();
        while (start >= 0) {
            template = template.substring(0, start) + s.get() + template.substring(start + len);
            start = template.indexOf(k);
        }
        return template;
    }

    private static String replaceAll(String template, String k, String v) {
        int start = template.indexOf(k);
        int len = k.length();
        while (start >= 0) {
            template = template.substring(0, start) + v + template.substring(start + len);
            start = template.indexOf(k);
        }
        return template;
    }
}
