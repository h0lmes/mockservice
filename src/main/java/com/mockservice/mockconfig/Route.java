package com.mockservice.mockconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {

    private static final String REQUEST_MAPPING_DELIMITER = "/";
    private static final String NAME_DELIMITER = "-";
    private static final String DATA_FILE_REGEX = "(get|post|put|patch|delete)(-)(.+)(--)(.+)(\\.json|.+\\.xml)$|(get|post|put|patch|delete)(-)(.+)(\\.json|.+\\.xml)$";

    private String path = "";
    private RequestMethod method = RequestMethod.GET;
    private RouteType type = RouteType.REST;
    private String suffix = "";
    private String response = "";
    private boolean disabled = false;
    private String group = ""; // non serializable

    public Route() {
        // default
    }

    public static Route fromFileName(String filename) {
        Pattern pattern = Pattern.compile(DATA_FILE_REGEX, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(filename);
        if (!matcher.find()) {
            return null;
        }

        String method = matcher.group(1) == null ? matcher.group(7) : matcher.group(1);
        String path = matcher.group(3) == null ? matcher.group(9) : matcher.group(3);
        String suffix = matcher.group(5) == null ? "" : matcher.group(5);
        String ext = matcher.group(6) == null ? matcher.group(10) : matcher.group(6);
        return new Route()
                .setType(RouteType.ofExt(ext))
                .setMethod(RequestMethod.valueOf(method.toUpperCase()))
                .setPath(path)
                .setSuffix(suffix);
    }

    public String getPath() {
        return path;
    }

    public Route setPath(String path) {
        this.path = path;
        return this;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public Route setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public RouteType getType() {
        return type;
    }

    public Route setType(RouteType type) {
        Objects.requireNonNull(type, "Route type can not be null");
        this.type = type;
        return this;
    }

    public String getSuffix() {
        return suffix;
    }

    public Route setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String getResponse() {
        return response;
    }

    public Route setResponse(String response) {
        this.response = response;
        return this;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public Route setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @JsonIgnore
    public String getGroup() {
        return group;
    }

    public Route setGroup(String group) {
        this.group = group;
        return this;
    }

    public String toFileName() {
        return getMethod().toString()
                + "-"
                + getEncodedPath()
                + getSuffix()
                + type.getExt();
    }

    public String toPathFileName() {
        return getGroup() + File.separator + toFileName();
    }

    private String getEncodedPath() {
        String result = path;
        if (result.startsWith(REQUEST_MAPPING_DELIMITER)) {
            result = result.substring(1);
        }
        String[] pathParts = result.split(REQUEST_MAPPING_DELIMITER);
        result = String.join(NAME_DELIMITER, pathParts);
        return result.toLowerCase();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, method, path, suffix);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Route)) return false;
        Route other = (Route) o;
        return type.equals(other.getType())
                && method.equals(other.getMethod())
                && path.equals(other.getPath())
                && suffix.equals(other.getSuffix());
    }

    @Override
    public  String toString() {
        return String.format("(path=%s, method=%s, type=%s, suffix=%s, disabled=%s, group=%s)", path, method, type, suffix, disabled, group);
    }
}
