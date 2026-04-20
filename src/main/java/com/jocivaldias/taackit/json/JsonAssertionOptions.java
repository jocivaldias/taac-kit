package com.jocivaldias.taackit.json;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public final class JsonAssertionOptions {

    private boolean strict = false;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<String> ignoredFields = new HashSet<>();

    private String comparatorName = "default";

    public static JsonAssertionOptions defaults() {
        return new JsonAssertionOptions();
    }

    public Set<String> getIgnoredFields() {
        return Collections.unmodifiableSet(ignoredFields);
    }

    public void setIgnoredFields(Set<String> ignoredFields) {
        this.ignoredFields = new HashSet<>(ignoredFields);
    }
}
