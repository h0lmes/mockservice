package com.mockservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TemplateService {

    public String resolve(String template, Map<String, String> vars) {
        for (Map.Entry<String, String> e : vars.entrySet()){
            template = template.replaceAll("\\$\\{" + e.getKey() + "\\}", e.getValue());
        }
        return template;
    }
}
