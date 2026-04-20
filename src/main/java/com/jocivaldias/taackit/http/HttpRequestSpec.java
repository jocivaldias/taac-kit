package com.jocivaldias.taackit.http;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.AccessLevel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public final class HttpRequestSpec {

    private String method;
    private String path;

    @Getter(AccessLevel.NONE)
    private Map<String, String> headers = new LinkedHashMap<>();

    @Getter(AccessLevel.NONE)
    private Map<String, String> queryParams = new LinkedHashMap<>();

    private JsonNode body;

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public HttpRequestSpec setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpRequestSpec setPath(String path) {
        this.path = path;
        return this;
    }

    public HttpRequestSpec setHeaders(Map<String, String> headers) {
        this.headers = new LinkedHashMap<>(headers);
        return this;
    }

    public HttpRequestSpec setQueryParams(Map<String, String> queryParams) {
        this.queryParams = new LinkedHashMap<>(queryParams);
        return this;
    }

    public HttpRequestSpec setBody(JsonNode body) {
        this.body = body;
        return this;
    }
}
