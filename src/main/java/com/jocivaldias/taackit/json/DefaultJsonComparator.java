package com.jocivaldias.taackit.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Set;

public class DefaultJsonComparator implements JsonComparator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void assertEquals(JsonNode expected, JsonNode actual, JsonAssertionOptions options) {
        JsonNode filteredExpected = applyIgnoredFields(expected, options.getIgnoredFields());
        JsonNode filteredActual = applyIgnoredFields(actual, options.getIgnoredFields());

        try {
            String expectedStr = objectMapper.writeValueAsString(filteredExpected);
            String actualStr = objectMapper.writeValueAsString(filteredActual);

            JSONCompareMode mode = options.isStrict() ? JSONCompareMode.STRICT : JSONCompareMode.LENIENT;
            JSONAssert.assertEquals(expectedStr, actualStr, mode);
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error comparing JSON", e);
        }
    }

    private JsonNode applyIgnoredFields(JsonNode node, Set<String> ignoredFields) {
        if (ignoredFields == null || ignoredFields.isEmpty() || node == null) {
            return node;
        }
        JsonNode copy = node.deepCopy();
        for (String field : ignoredFields) {
            removeFieldRecursively(copy, field);
        }
        return copy;
    }

    private void removeFieldRecursively(JsonNode node, String fieldName) {
        if (node.isObject()) {
            ((ObjectNode) node).remove(fieldName);
            node.elements().forEachRemaining(child -> removeFieldRecursively(child, fieldName));
        } else if (node.isArray()) {
            node.elements().forEachRemaining(child -> removeFieldRecursively(child, fieldName));
        }
    }
}
