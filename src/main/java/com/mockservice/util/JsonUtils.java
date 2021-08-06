package com.mockservice.util;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonUtils {
    
    private JsonUtils() {
        /* hidden */
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
                }
                else if (ch == 'n') builder.append('\n');
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
            }
        }

        return builder.toString();
    }

    public static boolean isJson(String value) {
        value = value.stripLeading();
        return value.startsWith("{") || value.startsWith("[") || value.startsWith("null");
    }

    public static void validate(String json, String schema) {
        JSONObject jsonSchema = new JSONObject(new JSONTokener(schema));
        JSONObject jsonSubject = new JSONObject(new JSONTokener(json));
        SchemaLoader.load(jsonSchema).validate(jsonSubject);
    }
}
