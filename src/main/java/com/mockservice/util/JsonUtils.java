package com.mockservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.IOException;

public class JsonUtils {

    private JsonUtils() {
        /* hidden */
    }

    public static String escape(String str) {
        String result = str;
        result = result.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        result = result.replace("\b", "\\b");
        result = result.replace("\f", "\\f");
        result = result.replace("\n", "\\n");
        result = result.replace("\r", "\\r");
        result = result.replace("\t", "\\t");
        // TODO: escape unicode
        return result;
    }

    public static String unescape(String str) {
        StringBuilder builder = new StringBuilder();
        int len = str.length();
        char[] chars = str.toCharArray();

        int i = 0;
        while (i < len) {
            char ch = chars[i++];

            if (ch == '\\') {

                if (i >= len) {
                    throw new IllegalArgumentException("Illegal escape sequence at the end of string.");
                }

                ch = chars[i++];

                if (ch == '\\' || ch == '/' || ch == '"' || ch == '\'') {
                    builder.append(ch);
                } else if (ch == 'n') builder.append('\n');
                else if (ch == 'r') builder.append('\r');
                else if (ch == 't') builder.append('\t');
                else if (ch == 'b') builder.append('\b');
                else if (ch == 'f') builder.append('\f');
                else if (ch == 'u') {
                    if (i + 4 > len) {
                        throw new IllegalArgumentException("Malformed unicode char at " + i + ". Must be \\uXXXX.");
                    }
                    int charCode = Integer.parseInt("" + chars[i] + chars[i + 1] + chars[i + 2] + chars[i + 3], 16);
                    builder.append((char) charCode);
                    i += 4;
                } else {
                    throw new IllegalArgumentException("Illegal escape sequence: \\" + ch);
                }
            } else {
                builder.append(ch);
            }
        }

        return builder.toString();
    }

    public static boolean isJson(String value) {
        value = value.stripLeading();
        return value.startsWith("{") || value.startsWith("[") || value.startsWith("null");
    }

    public static void validate(String json, String schema) {
        final JsonNode nodeSchema;
        final JsonNode nodeJson;
        try {
            nodeSchema = JsonLoader.fromString(schema);
            nodeJson = JsonLoader.fromString(json);
        } catch (IOException e) {
            throw new JsonValidationException(e);
        }
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema jsonSchema;
        try {
            jsonSchema = factory.getJsonSchema(nodeSchema);
        } catch (ProcessingException e) {
            throw new JsonValidationException(e);
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
            throw new JsonValidationException(builder.toString());
        }
    }
}
