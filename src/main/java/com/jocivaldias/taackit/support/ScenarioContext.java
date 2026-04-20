package com.jocivaldias.taackit.support;

import com.jocivaldias.taackit.http.HttpRequestSpec;
import com.jocivaldias.taackit.json.JsonAssertionOptions;
import io.restassured.response.Response;

public final class ScenarioContext {

    private static final ThreadLocal<ScenarioContext> CURRENT =
        ThreadLocal.withInitial(ScenarioContext::new);

    private HttpRequestSpec currentRequestSpec;
    private Response lastHttpResponse;
    private JsonAssertionOptions jsonOptions = JsonAssertionOptions.defaults();

    private ScenarioContext() {
    }

    public static ScenarioContext current() {
        return CURRENT.get();
    }

    public static void reset() {
        CURRENT.set(new ScenarioContext());
    }

    public HttpRequestSpec getCurrentRequestSpec() {
        return currentRequestSpec;
    }

    public void setCurrentRequestSpec(HttpRequestSpec currentRequestSpec) {
        this.currentRequestSpec = currentRequestSpec;
    }

    public Response getLastHttpResponse() {
        return lastHttpResponse;
    }

    public void setLastHttpResponse(Response lastHttpResponse) {
        this.lastHttpResponse = lastHttpResponse;
    }

    public JsonAssertionOptions getJsonOptions() {
        return jsonOptions;
    }
}
