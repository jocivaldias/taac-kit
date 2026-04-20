package com.jocivaldias.taackit.json;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonComparator {

    void assertEquals(JsonNode expected, JsonNode actual, JsonAssertionOptions options);
}
