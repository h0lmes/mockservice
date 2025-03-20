package com.mockservice.domain;

import com.mockservice.template.MockVariables;

import java.util.Objects;

public class VariableMatcher {

    private String variableName = "";
    private String variableValue = "";

    public VariableMatcher(String matcherCondition) {
        setCondition(matcherCondition);
    }

    public VariableMatcher setCondition(String matcherCondition) {
        variableName = "";
        variableValue = "";
        int pos = matcherCondition.indexOf("=");
        if (pos > 0) {
            variableName = matcherCondition.substring(0, pos).trim();
            variableValue = matcherCondition.substring(pos + 1).trim();
            if (variableValue.startsWith("\"") && variableValue.endsWith("\"")) {
                variableValue = variableValue.substring(1, variableValue.length() - 1);
            }
        }
        return this;
    }

    public boolean match(MockVariables variables) {
        if (variableName.isEmpty()) {
            return false;
        }

        return Objects.equals(variables.get(variableName), variableValue);
    }
}
