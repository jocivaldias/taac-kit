package com.jocivaldias.taackit.http;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest(httpPort = 8089)
class RestAssuredHttpTestClientTest {

    private RestAssuredHttpTestClient client;

    @BeforeEach
    void setUp() {
        client = new RestAssuredHttpTestClient("http://localhost:8089");
    }

    @Test
    void execute_get_returnsResponse() {
        stubFor(get(urlEqualTo("/api/users"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"total\": 0}")));

        HttpRequestSpec spec = new HttpRequestSpec().setMethod("GET").setPath("/api/users");
        Response response = client.execute(spec);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void execute_post_sendsBodyAndReturnsResponse() {
        stubFor(post(urlEqualTo("/api/users"))
            .withRequestBody(containing("\"name\":\"João\""))
            .willReturn(aResponse().withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 1}")));

        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "João");

        HttpRequestSpec spec = new HttpRequestSpec()
            .setMethod("POST")
            .setPath("/api/users")
            .setBody(body);

        Response response = client.execute(spec);
        assertEquals(201, response.getStatusCode());
    }

    @Test
    void execute_put_sendsRequest() {
        stubFor(put(urlEqualTo("/api/users/1"))
            .willReturn(aResponse().withStatus(200)));

        HttpRequestSpec spec = new HttpRequestSpec().setMethod("PUT").setPath("/api/users/1");
        Response response = client.execute(spec);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void execute_delete_sendsRequest() {
        stubFor(delete(urlEqualTo("/api/users/1"))
            .willReturn(aResponse().withStatus(204)));

        HttpRequestSpec spec = new HttpRequestSpec().setMethod("DELETE").setPath("/api/users/1");
        Response response = client.execute(spec);

        assertEquals(204, response.getStatusCode());
    }

    @Test
    void execute_patch_sendsRequest() {
        stubFor(patch(urlEqualTo("/api/users/1"))
            .willReturn(aResponse().withStatus(200)));

        HttpRequestSpec spec = new HttpRequestSpec().setMethod("PATCH").setPath("/api/users/1");
        Response response = client.execute(spec);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void execute_sendsCustomHeaders() {
        stubFor(get(urlEqualTo("/api/secure"))
            .withHeader("Authorization", equalTo("Bearer token123"))
            .willReturn(aResponse().withStatus(200)));

        HttpRequestSpec spec = new HttpRequestSpec()
            .setMethod("GET")
            .setPath("/api/secure")
            .setHeaders(Map.of("Authorization", "Bearer token123"));

        Response response = client.execute(spec);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void execute_sendsQueryParams() {
        stubFor(get(urlPathEqualTo("/api/users"))
            .withQueryParam("page", equalTo("2"))
            .withQueryParam("size", equalTo("10"))
            .willReturn(aResponse().withStatus(200)));

        HttpRequestSpec spec = new HttpRequestSpec()
            .setMethod("GET")
            .setPath("/api/users")
            .setQueryParams(Map.of("page", "2", "size", "10"));

        Response response = client.execute(spec);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void execute_unsupportedMethod_throwsIllegalArgumentException() {
        HttpRequestSpec spec = new HttpRequestSpec().setMethod("TRACE").setPath("/");

        assertThrows(IllegalArgumentException.class, () -> client.execute(spec));
    }

    @Test
    void execute_customContentTypeOverridesDefault() {
        stubFor(post(urlEqualTo("/api/data"))
            .withHeader("Content-Type", containing("text/plain"))
            .willReturn(aResponse().withStatus(200)));

        HttpRequestSpec spec = new HttpRequestSpec()
            .setMethod("POST")
            .setPath("/api/data")
            .setHeaders(Map.of("Content-Type", "text/plain"));

        Response response = client.execute(spec);
        assertEquals(200, response.getStatusCode());
    }

    private void assertThrows(Class<IllegalArgumentException> clazz, Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("Expected " + clazz.getSimpleName() + " to be thrown");
        } catch (Exception e) {
            if (!clazz.isInstance(e)) {
                throw new AssertionError("Expected " + clazz.getSimpleName() + " but got " + e.getClass().getSimpleName(), e);
            }
        }
    }
}
