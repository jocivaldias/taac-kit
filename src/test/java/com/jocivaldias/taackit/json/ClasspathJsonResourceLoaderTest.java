package com.jocivaldias.taackit.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClasspathJsonResourceLoaderTest {

    private ClasspathJsonResourceLoader loader;

    @BeforeEach
    void setUp() {
        loader = new ClasspathJsonResourceLoader("payloads");
    }

    @Test
    void load_byId_loadsJsonFile() {
        JsonNode node = loader.load("simple-body");

        assertNotNull(node);
        assertEquals("João", node.get("name").asText());
    }

    @Test
    void load_byFullClasspathPath_loadsJsonFile() {
        JsonNode node = loader.load("payloads/simple-body.json");

        assertNotNull(node);
        assertEquals("João", node.get("name").asText());
    }

    @Test
    void load_requestEnvelope_loadsAllFields() {
        JsonNode node = loader.load("request-envelope");

        assertNotNull(node);
        assertTrue(node.has("headers"));
        assertTrue(node.has("queryParams"));
        assertTrue(node.has("body"));
        assertEquals("Bearer token123", node.get("headers").get("Authorization").asText());
        assertEquals("1", node.get("queryParams").get("page").asText());
        assertEquals("João", node.get("body").get("name").asText());
    }

    @Test
    void load_nonExistentFile_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> loader.load("does-not-exist"));
    }

    @Test
    void load_invalidJson_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> loader.load("invalid-json"));
    }
}
