package com.jocivaldias.taackit.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultJsonComparatorTest {

    private DefaultJsonComparator comparator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        comparator = new DefaultJsonComparator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void assertEquals_identicalObjects_passes() throws Exception {
        JsonNode expected = objectMapper.readTree("{\"name\":\"João\",\"age\":30}");
        JsonNode actual = objectMapper.readTree("{\"name\":\"João\",\"age\":30}");

        assertDoesNotThrow(() -> comparator.assertEquals(expected, actual, JsonAssertionOptions.defaults()));
    }

    @Test
    void assertEquals_lenient_allowsExtraFieldsInActual() throws Exception {
        JsonNode expected = objectMapper.readTree("{\"name\":\"João\"}");
        JsonNode actual = objectMapper.readTree("{\"name\":\"João\",\"extra\":\"field\"}");

        JsonAssertionOptions opts = JsonAssertionOptions.defaults();
        opts.setStrict(false);

        assertDoesNotThrow(() -> comparator.assertEquals(expected, actual, opts));
    }

    @Test
    void assertEquals_strict_failsOnExtraFieldsInActual() throws Exception {
        JsonNode expected = objectMapper.readTree("{\"name\":\"João\"}");
        JsonNode actual = objectMapper.readTree("{\"name\":\"João\",\"extra\":\"field\"}");

        JsonAssertionOptions opts = JsonAssertionOptions.defaults();
        opts.setStrict(true);

        assertThrows(AssertionError.class, () -> comparator.assertEquals(expected, actual, opts));
    }

    @Test
    void assertEquals_mismatchedValue_fails() throws Exception {
        JsonNode expected = objectMapper.readTree("{\"name\":\"João\"}");
        JsonNode actual = objectMapper.readTree("{\"name\":\"Maria\"}");

        assertThrows(AssertionError.class,
            () -> comparator.assertEquals(expected, actual, JsonAssertionOptions.defaults()));
    }

    @Test
    void assertEquals_ignoredFieldsRemovedFromBothSides() throws Exception {
        JsonNode expected = objectMapper.readTree("{\"id\":\"ignored\",\"name\":\"João\"}");
        JsonNode actual = objectMapper.readTree("{\"id\":\"different\",\"name\":\"João\"}");

        JsonAssertionOptions opts = JsonAssertionOptions.defaults();
        opts.setIgnoredFields(Set.of("id"));

        assertDoesNotThrow(() -> comparator.assertEquals(expected, actual, opts));
    }

    @Test
    void assertEquals_ignoredFieldsNested() throws Exception {
        JsonNode expected = objectMapper.readTree("{\"data\":{\"id\":\"x\",\"name\":\"João\"}}");
        JsonNode actual = objectMapper.readTree("{\"data\":{\"id\":\"y\",\"name\":\"João\"}}");

        JsonAssertionOptions opts = JsonAssertionOptions.defaults();
        opts.setIgnoredFields(Set.of("id"));

        assertDoesNotThrow(() -> comparator.assertEquals(expected, actual, opts));
    }

    @Test
    void assertEquals_ignoredFieldInArray() throws Exception {
        JsonNode expected = objectMapper.readTree("[{\"id\":\"x\",\"name\":\"João\"}]");
        JsonNode actual = objectMapper.readTree("[{\"id\":\"y\",\"name\":\"João\"}]");

        JsonAssertionOptions opts = JsonAssertionOptions.defaults();
        opts.setIgnoredFields(Set.of("id"));

        assertDoesNotThrow(() -> comparator.assertEquals(expected, actual, opts));
    }

    @Test
    void assertEquals_jsonArray_passes() throws Exception {
        JsonNode expected = objectMapper.readTree("[1,2,3]");
        JsonNode actual = objectMapper.readTree("[1,2,3]");

        assertDoesNotThrow(() -> comparator.assertEquals(expected, actual, JsonAssertionOptions.defaults()));
    }

    @Test
    void assertEquals_jsonArray_fails() throws Exception {
        JsonNode expected = objectMapper.readTree("[1,2,3]");
        JsonNode actual = objectMapper.readTree("[1,2,4]");

        assertThrows(AssertionError.class,
            () -> comparator.assertEquals(expected, actual, JsonAssertionOptions.defaults()));
    }

    @Test
    void assertEquals_noIgnoredFields_doesNotMutateInput() throws Exception {
        JsonNode expected = objectMapper.readTree("{\"id\":\"1\",\"name\":\"João\"}");
        JsonNode actual = objectMapper.readTree("{\"id\":\"1\",\"name\":\"João\"}");

        comparator.assertEquals(expected, actual, JsonAssertionOptions.defaults());

        assertTrue(expected.has("id"));
        assertTrue(actual.has("id"));
    }
}
