package com.mockservice.template;

import com.mockservice.util.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHeadersTemplate {

    private static final String HTTP_HEADER_DELIMITER = ":";
    private static final int HTTP_HEADER_DELIMITER_LEN = HTTP_HEADER_DELIMITER.length();
    private Map<String, List<StringTemplate>> templateMap;
    private Map<String, List<String>> resultMap;
    private boolean containsTemplates = false;

    public RequestHeadersTemplate(String headers) {
        readHeaders(headers);
        if (!containsTemplates) generateResult();
    }

    private void readHeaders(String headers) {
        if (headers.trim().isEmpty()) return;

        List<String> lines = IOUtils.toList(headers);
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                if (templateMap == null) templateMap = new HashMap<>();

                int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
                String key = line.substring(0, delimiter).trim();
                String value = line.substring(delimiter + HTTP_HEADER_DELIMITER_LEN).trim();

                var list = templateMap.getOrDefault(key, new ArrayList<>());
                StringTemplate template = new StringTemplate(value);
                if (template.containsTokens()) containsTemplates = true;
                list.add(template);
                templateMap.put(key, list);
            }
        }
    }

    private void generateResult() {
        if (templateMap == null) return;
        for (String key : templateMap.keySet()) {
            generateResultForKey(key);
        }
    }

    private void generateResultForKey(String key) {
        if (resultMap == null) resultMap = new HashMap<>();

        var templateList = templateMap.get(key);
        if (templateList == null) return;
        var resultList = resultMap.getOrDefault(key, new ArrayList<>());
        templateList.forEach(t -> resultList.add(t.toString()));
        resultMap.put(key, resultList);
    }

    private void updateResult(MockVariables variables, MockFunctions functions) {
        if (templateMap == null) return;
        for (String key : templateMap.keySet()) {
            updateResultForKey(key, variables, functions);
        }
    }

    private void updateResultForKey(String key, MockVariables variables, MockFunctions functions) {
        if (resultMap == null) resultMap = new HashMap<>();

        var templateList = templateMap.get(key);
        if (templateList == null) return;

        var resultList = resultMap.getOrDefault(key, new ArrayList<>());
        int i = 0;
        for (StringTemplate t : templateList) {
            resultList.set(i, t.toString(variables, functions));
            i++;
        }
        resultMap.put(key, resultList);
    }

    public Map<String, List<String>> toMap(MockVariables variables, MockFunctions functions) {
        if (containsTemplates) updateResult(variables, functions);
        return resultMap != null ? resultMap : Map.of();
    }
}
