package com.mockachu.validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.IOException;

public class JsonDataValidator implements DataValidator {

    private final JsonSchemaFactory jsonSchemaFactory;

    public JsonDataValidator(JsonSchemaFactory jsonSchemaFactory) {
        this.jsonSchemaFactory = jsonSchemaFactory;
    }

    @Override
    public boolean applicable(String data) {
        try {
            JsonLoader.fromString(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void validate(String data, String schema) {
        final JsonNode nodeSchema;
        final JsonNode nodeJson;
        try {
            nodeSchema = JsonLoader.fromString(schema);
            nodeJson = JsonLoader.fromString(data);
        } catch (IOException e) {
            throw new DataValidationException("Error loading data or schema.", e);
        }
        final JsonSchema jsonSchema;
        try {
            jsonSchema = jsonSchemaFactory.getJsonSchema(nodeSchema);
        } catch (ProcessingException e) {
            throw new DataValidationException(e);
        }

        ProcessingReport report = jsonSchema.validateUnchecked(nodeJson);

        if (report != null && !report.isSuccess()) {
            StringBuilder builder = new StringBuilder();
            report.forEach(message -> {
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append(message.asJson().toString());
            });
            throw new DataValidationException(builder.toString());
        }
    }
}
