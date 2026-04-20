package com.jocivaldias.taackit.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ClasspathJsonResourceLoader implements JsonResourceLoader {

    private final String baseDir;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClasspathJsonResourceLoader(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public JsonNode load(String id) {
        String path = id.endsWith(".json")
            ? (id.startsWith("/") ? id.substring(1) : id)
            : baseDir + "/" + id + ".json";

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClasspathJsonResourceLoader.class.getClassLoader();
        }

        try (InputStream is = cl.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("JSON resource not found on classpath: " + path);
            }
            return objectMapper.readTree(is);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON resource: " + path, e);
        }
    }
}
