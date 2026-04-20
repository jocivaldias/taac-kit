package com.jocivaldias.taackit.support;

import com.jocivaldias.taackit.http.HttpRequestSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ScenarioContextTest {

    @BeforeEach
    void setUp() {
        ScenarioContext.reset();
    }

    @Test
    void current_returnsSameInstanceOnSameThread() {
        ScenarioContext first = ScenarioContext.current();
        ScenarioContext second = ScenarioContext.current();

        assertSame(first, second);
    }

    @Test
    void reset_createsNewInstance() {
        ScenarioContext before = ScenarioContext.current();
        ScenarioContext.reset();
        ScenarioContext after = ScenarioContext.current();

        assertNotSame(before, after);
    }

    @Test
    void reset_clearsState() {
        ScenarioContext.current().setCurrentRequestSpec(new HttpRequestSpec());
        ScenarioContext.reset();

        assertNull(ScenarioContext.current().getCurrentRequestSpec());
    }

    @Test
    void jsonOptions_defaultsToNonStrictAndDefaultComparator() {
        assertFalse(ScenarioContext.current().getJsonOptions().isStrict());
        assertEquals("default", ScenarioContext.current().getJsonOptions().getComparatorName());
        assertTrue(ScenarioContext.current().getJsonOptions().getIgnoredFields().isEmpty());
    }

    @Test
    void setCurrentRequestSpec_storesAndRetrieves() {
        HttpRequestSpec spec = new HttpRequestSpec().setMethod("GET").setPath("/test");
        ScenarioContext.current().setCurrentRequestSpec(spec);

        assertSame(spec, ScenarioContext.current().getCurrentRequestSpec());
    }

    @Test
    void setLastHttpResponse_storesAndRetrieves() {
        var mockResponse = mock(io.restassured.response.Response.class);
        ScenarioContext.current().setLastHttpResponse(mockResponse);

        assertSame(mockResponse, ScenarioContext.current().getLastHttpResponse());
    }
}
