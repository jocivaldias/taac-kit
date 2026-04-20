package com.jocivaldias.taackit.json;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonResourceLoader {

    JsonNode load(String id);
}
