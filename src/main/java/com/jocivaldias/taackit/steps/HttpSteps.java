package com.jocivaldias.taackit.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jocivaldias.taackit.core.IntegrationTestRuntime;
import com.jocivaldias.taackit.http.HttpRequestSpec;
import com.jocivaldias.taackit.json.ClasspathJsonResourceLoader;
import com.jocivaldias.taackit.json.JsonComparator;
import com.jocivaldias.taackit.json.JsonComparatorRegistry;
import com.jocivaldias.taackit.json.JsonResourceLoader;
import com.jocivaldias.taackit.support.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpSteps {

    private final JsonResourceLoader jsonLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpSteps() {
        this.jsonLoader = new ClasspathJsonResourceLoader("payloads");
    }

    HttpSteps(JsonResourceLoader jsonLoader) {
        this.jsonLoader = jsonLoader;
    }

    @Dado("o endpoint {string} com o verbo {string}")
    public void oEndpoint(String path, String method) {
        ScenarioContext.current().setCurrentRequestSpec(
            new HttpRequestSpec().setMethod(method).setPath(path));
    }

    @Quando("envia a requisicao {string}")
    public void enviaARequisicao(String requestId) {
        HttpRequestSpec spec = ScenarioContext.current().getCurrentRequestSpec();
        applyEnvelope(jsonLoader.load(requestId), spec);
        ScenarioContext.current().setLastHttpResponse(
            IntegrationTestRuntime.httpClient().execute(spec));
    }

    @Quando("envia a requisicao sem corpo")
    public void enviaARequisicaoSemCorpo() {
        ScenarioContext.current().setLastHttpResponse(
            IntegrationTestRuntime.httpClient().execute(
                ScenarioContext.current().getCurrentRequestSpec()));
    }

    @Entao("devera retornar status {int}")
    public void deveraRetornarStatus(int expected) {
        int actual = ScenarioContext.current().getLastHttpResponse().getStatusCode();
        if (expected != actual) {
            throw new AssertionError(
                "HTTP status code diferente do esperado. Expected: " + expected + " but was: " + actual);
        }
    }

    @Entao("a resposta deve conter {string}")
    public void aRespostaDeveConter(String responseId) throws Exception {
        Response response = ScenarioContext.current().getLastHttpResponse();
        JsonNode actual = objectMapper.readTree(response.asString());
        JsonNode expected = jsonLoader.load(responseId);

        JsonComparator comparator = JsonComparatorRegistry.get(
            ScenarioContext.current().getJsonOptions().getComparatorName());
        comparator.assertEquals(expected, actual, ScenarioContext.current().getJsonOptions());
    }

    private void applyEnvelope(JsonNode raw, HttpRequestSpec spec) {
        if (!isEnvelope(raw)) {
            spec.setBody(raw);
            return;
        }
        if (raw.has("headers") && raw.get("headers").isObject()) {
            Map<String, String> headers = new LinkedHashMap<>();
            raw.get("headers").fields().forEachRemaining(
                e -> headers.put(e.getKey(), e.getValue().asText()));
            spec.setHeaders(headers);
        }
        if (raw.has("queryParams") && raw.get("queryParams").isObject()) {
            Map<String, String> params = new LinkedHashMap<>();
            raw.get("queryParams").fields().forEachRemaining(
                e -> params.put(e.getKey(), e.getValue().asText()));
            spec.setQueryParams(params);
        }
        if (raw.has("body") && !raw.get("body").isNull()) {
            spec.setBody(raw.get("body"));
        }
    }

    private boolean isEnvelope(JsonNode node) {
        return node.isObject()
            && (node.has("body") || node.has("headers") || node.has("queryParams"));
    }
}
