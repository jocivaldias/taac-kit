package com.jocivaldias.taackit.http;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestSpecTest {

    @Test
    void setMethod_returnsFluentSelf() {
        HttpRequestSpec spec = new HttpRequestSpec();
        assertSame(spec, spec.setMethod("POST"));
    }

    @Test
    void setPath_returnsFluentSelf() {
        HttpRequestSpec spec = new HttpRequestSpec();
        assertSame(spec, spec.setPath("/api/users"));
    }

    @Test
    void getHeaders_returnsImmutableCopy() {
        HttpRequestSpec spec = new HttpRequestSpec();
        spec.setHeaders(Map.of("X-Key", "value"));

        assertThrows(UnsupportedOperationException.class,
            () -> spec.getHeaders().put("other", "val"));
    }

    @Test
    void getQueryParams_returnsImmutableCopy() {
        HttpRequestSpec spec = new HttpRequestSpec();
        spec.setQueryParams(Map.of("page", "1"));

        assertThrows(UnsupportedOperationException.class,
            () -> spec.getQueryParams().put("other", "val"));
    }

    @Test
    void allFields_setAndGet_correctly() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("name", "test");

        HttpRequestSpec spec = new HttpRequestSpec()
            .setMethod("PUT")
            .setPath("/api/items/1")
            .setHeaders(Map.of("Authorization", "Bearer token"))
            .setQueryParams(Map.of("version", "2"))
            .setBody(body);

        assertEquals("PUT", spec.getMethod());
        assertEquals("/api/items/1", spec.getPath());
        assertEquals("Bearer token", spec.getHeaders().get("Authorization"));
        assertEquals("2", spec.getQueryParams().get("version"));
        assertEquals(body, spec.getBody());
    }

    @Test
    void defaultHeaders_isEmpty() {
        HttpRequestSpec spec = new HttpRequestSpec();
        assertTrue(spec.getHeaders().isEmpty());
    }

    @Test
    void defaultQueryParams_isEmpty() {
        HttpRequestSpec spec = new HttpRequestSpec();
        assertTrue(spec.getQueryParams().isEmpty());
    }
}
