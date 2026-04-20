package com.jocivaldias.taackit.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jocivaldias.taackit.core.IntegrationTestRuntime;
import com.jocivaldias.taackit.core.RuntimeConfig;
import com.jocivaldias.taackit.http.HttpRequestSpec;
import com.jocivaldias.taackit.http.HttpTestClient;
import com.jocivaldias.taackit.json.JsonResourceLoader;
import com.jocivaldias.taackit.support.ScenarioContext;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpStepsTest {

    private HttpTestClient httpClient;
    private JsonResourceLoader jsonLoader;
    private HttpSteps steps;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpTestClient.class);
        jsonLoader = mock(JsonResourceLoader.class);
        steps = new HttpSteps(jsonLoader);
        objectMapper = new ObjectMapper();

        IntegrationTestRuntime.configure(RuntimeConfig.builder().httpTestClient(httpClient).build());
        ScenarioContext.reset();
    }

    @AfterEach
    void tearDown() {
        IntegrationTestRuntime.configure(RuntimeConfig.builder().build());
    }

    @Test
    void oEndpoint_setsPathAndMethodOnContext() {
        steps.oEndpoint("/api/users", "POST");

        HttpRequestSpec spec = ScenarioContext.current().getCurrentRequestSpec();
        assertNotNull(spec);
        assertEquals("/api/users", spec.getPath());
        assertEquals("POST", spec.getMethod());
    }

    @Test
    void enviaARequisicaoSemCorpo_executesSpecWithoutBody() {
        steps.oEndpoint("/api/users", "GET");
        Response resp = mockResponse(200, "{}");
        when(httpClient.execute(any(HttpRequestSpec.class))).thenReturn(resp);

        steps.enviaARequisicaoSemCorpo();

        verify(httpClient).execute(any(HttpRequestSpec.class));
        assertNotNull(ScenarioContext.current().getLastHttpResponse());
    }

    @Test
    void enviaARequisicao_withPlainBody_setsBodyOnSpec() throws Exception {
        steps.oEndpoint("/api/users", "POST");
        JsonNode body = objectMapper.readTree("{\"name\":\"João\"}");
        when(jsonLoader.load("create-user")).thenReturn(body);
        Response resp = mockResponse(201, "{\"id\":1}");
        when(httpClient.execute(any(HttpRequestSpec.class))).thenReturn(resp);

        steps.enviaARequisicao("create-user");

        HttpRequestSpec spec = ScenarioContext.current().getCurrentRequestSpec();
        assertNotNull(spec.getBody());
        assertEquals("João", spec.getBody().get("name").asText());
    }

    @Test
    void enviaARequisicao_withEnvelope_setsHeadersQueryParamsAndBody() throws Exception {
        steps.oEndpoint("/api/users", "GET");
        JsonNode envelope = objectMapper.readTree(
            "{\"headers\":{\"Authorization\":\"Bearer tok\"}," +
            "\"queryParams\":{\"page\":\"1\"}," +
            "\"body\":{\"name\":\"João\"}}");
        when(jsonLoader.load("envelope")).thenReturn(envelope);
        Response resp = mockResponse(200, "{}");
        when(httpClient.execute(any(HttpRequestSpec.class))).thenReturn(resp);

        steps.enviaARequisicao("envelope");

        HttpRequestSpec spec = ScenarioContext.current().getCurrentRequestSpec();
        assertEquals("Bearer tok", spec.getHeaders().get("Authorization"));
        assertEquals("1", spec.getQueryParams().get("page"));
        assertEquals("João", spec.getBody().get("name").asText());
    }

    @Test
    void enviaARequisicao_envelopeWithoutBody_noBodyOnSpec() throws Exception {
        steps.oEndpoint("/api/items", "GET");
        JsonNode envelope = objectMapper.readTree("{\"queryParams\":{\"active\":\"true\"}}");
        when(jsonLoader.load("query-only")).thenReturn(envelope);
        Response resp = mockResponse(200, "[]");
        when(httpClient.execute(any(HttpRequestSpec.class))).thenReturn(resp);

        steps.enviaARequisicao("query-only");

        HttpRequestSpec spec = ScenarioContext.current().getCurrentRequestSpec();
        assertNull(spec.getBody());
        assertEquals("true", spec.getQueryParams().get("active"));
    }

    @Test
    void deveraRetornarStatus_matchingStatus_passes() {
        steps.oEndpoint("/api", "GET");
        Response resp = mockResponse(200, "{}");
        when(httpClient.execute(any())).thenReturn(resp);
        steps.enviaARequisicaoSemCorpo();

        assertDoesNotThrow(() -> steps.deveraRetornarStatus(200));
    }

    @Test
    void deveraRetornarStatus_wrongStatus_fails() {
        steps.oEndpoint("/api", "GET");
        Response resp = mockResponse(404, "{}");
        when(httpClient.execute(any())).thenReturn(resp);
        steps.enviaARequisicaoSemCorpo();

        assertThrows(AssertionError.class, () -> steps.deveraRetornarStatus(200));
    }

    @Test
    void aRespostaDeveConter_matchingJson_passes() throws Exception {
        steps.oEndpoint("/api/users", "GET");
        Response resp = mockResponse(200, "{\"name\":\"João\"}");
        when(httpClient.execute(any())).thenReturn(resp);
        steps.enviaARequisicaoSemCorpo();

        JsonNode expected = objectMapper.readTree("{\"name\":\"João\"}");
        when(jsonLoader.load("expected")).thenReturn(expected);

        assertDoesNotThrow(() -> steps.aRespostaDeveConter("expected"));
    }

    @Test
    void aRespostaDeveConter_mismatch_throws() throws Exception {
        steps.oEndpoint("/api/users", "GET");
        Response resp = mockResponse(200, "{\"name\":\"Maria\"}");
        when(httpClient.execute(any())).thenReturn(resp);
        steps.enviaARequisicaoSemCorpo();

        JsonNode expected = objectMapper.readTree("{\"name\":\"João\"}");
        when(jsonLoader.load("expected")).thenReturn(expected);

        assertThrows(AssertionError.class, () -> steps.aRespostaDeveConter("expected"));
    }

    private Response mockResponse(int status, String body) {
        Response response = mock(Response.class);
        when(response.getStatusCode()).thenReturn(status);
        when(response.asString()).thenReturn(body);
        return response;
    }
}
