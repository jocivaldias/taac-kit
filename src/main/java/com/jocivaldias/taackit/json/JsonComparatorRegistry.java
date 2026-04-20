package com.jocivaldias.taackit.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonComparatorRegistry {

    private static final Map<String, JsonComparator> REGISTRY = new ConcurrentHashMap<>();

    static {
        REGISTRY.put("default", new DefaultJsonComparator());
    }

    private JsonComparatorRegistry() {
    }

    public static void register(String name, JsonComparator comparator) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Comparator name must not be blank");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null");
        }
        REGISTRY.put(name, comparator);
    }

    public static JsonComparator get(String name) {
        JsonComparator comparator = REGISTRY.get(name);
        if (comparator == null) {
            comparator = REGISTRY.get("default");
        }
        return comparator;
    }
}
