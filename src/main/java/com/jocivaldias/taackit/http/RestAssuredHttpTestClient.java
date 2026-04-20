package com.jocivaldias.taackit.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

public class RestAssuredHttpTestClient implements HttpTestClient {

    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestAssuredHttpTestClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Response execute(HttpRequestSpec spec) {
        RequestSpecification request = RestAssured
            .given()
            .baseUri(baseUrl)
            .relaxedHTTPSValidation();

        Map<String, String> headers = buildHeaders(spec.getHeaders());
        headers.forEach(request::header);

        if (!spec.getQueryParams().isEmpty()) {
            request.queryParams(spec.getQueryParams());
        }

        if (spec.getBody() != null) {
            try {
                request.body(objectMapper.writeValueAsString(spec.getBody()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize request body", e);
            }
        }

        String method = spec.getMethod() != null ? spec.getMethod().toUpperCase() : "GET";
        String path = spec.getPath() != null ? spec.getPath() : "/";

        switch (method) {
            case "GET":    return request.get(path);
            case "POST":   return request.post(path);
            case "PUT":    return request.put(path);
            case "DELETE": return request.delete(path);
            case "PATCH":  return request.patch(path);
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    private Map<String, String> buildHeaders(Map<String, String> specHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.putAll(specHeaders);
        return headers;
    }
}
