package com.jocivaldias.taackit.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jocivaldias.taackit.core.IntegrationTestRuntime;
import com.jocivaldias.taackit.core.RuntimeConfig;
import com.jocivaldias.taackit.json.JsonResourceLoader;
import com.jocivaldias.taackit.messaging.QueueClient;
import com.jocivaldias.taackit.support.ScenarioContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessagingStepsTest {

    private static final String QUEUE = "http://localhost:4566/000000000000/test-queue";

    private QueueClient queueClient;
    private JsonResourceLoader jsonLoader;
    private MessagingSteps steps;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        queueClient = mock(QueueClient.class);
        jsonLoader = mock(JsonResourceLoader.class);
        steps = new MessagingSteps(jsonLoader);
        objectMapper = new ObjectMapper();

        IntegrationTestRuntime.configure(RuntimeConfig.builder().queueClient(queueClient).build());
        ScenarioContext.reset();
    }

    @AfterEach
    void tearDown() {
        IntegrationTestRuntime.configure(RuntimeConfig.builder().build());
    }

    @Test
    void enviaMensagemNaFila_withPlainBody_sendsJsonDirectly() throws Exception {
        JsonNode payload = objectMapper.readTree("{\"userId\":\"123\"}");
        when(jsonLoader.load("input")).thenReturn(payload);

        steps.enviaMensagemNaFila(QUEUE, "input");

        verify(queueClient).sendMessage(eq(QUEUE), contains("userId"), any());
    }

    @Test
    void enviaMensagemNaFila_withEnvelope_extractsBody() throws Exception {
        JsonNode envelope = objectMapper.readTree(
            "{\"body\":{\"userId\":\"123\"},\"attributes\":{}}");
        when(jsonLoader.load("input-envelope")).thenReturn(envelope);

        steps.enviaMensagemNaFila(QUEUE, "input-envelope");

        verify(queueClient).sendMessage(eq(QUEUE), contains("userId"), any());
    }

    @Test
    void enviaMensagemNaFila_withAttributes_passesThemToClient() throws Exception {
        JsonNode envelope = objectMapper.readTree(
            "{\"body\":{\"id\":1},\"attributes\":{" +
            "\"eventType\":{\"type\":\"String\",\"value\":\"ORDER_PLACED\"}}}");
        when(jsonLoader.load("order-event")).thenReturn(envelope);

        steps.enviaMensagemNaFila(QUEUE, "order-event");

        verify(queueClient).sendMessage(eq(QUEUE), any(),
            argThat(attrs -> attrs.containsKey("eventType") &&
                "ORDER_PLACED".equals(attrs.get("eventType").getValue())));
    }

    @Test
    void aguarda_doesNotThrow() {
        assertDoesNotThrow(() -> steps.aguarda("50ms"));
    }

    @Test
    void aFilaDeveConter_matchingJson_passes() throws Exception {
        when(queueClient.receiveMessage(QUEUE)).thenReturn("{\"status\":\"OK\"}");
        JsonNode expected = objectMapper.readTree("{\"status\":\"OK\"}");
        when(jsonLoader.load("expected-output")).thenReturn(expected);

        assertDoesNotThrow(() -> steps.aFilaDeveConter(QUEUE, "expected-output"));
    }

    @Test
    void aFilaDeveConter_noMessage_fails() {
        when(queueClient.receiveMessage(QUEUE)).thenReturn(null);
        when(jsonLoader.load("expected")).thenReturn(mock(JsonNode.class));

        assertThrows(AssertionError.class, () -> steps.aFilaDeveConter(QUEUE, "expected"));
    }

    @Test
    void aFilaDeveConter_mismatchedJson_throws() throws Exception {
        when(queueClient.receiveMessage(QUEUE)).thenReturn("{\"status\":\"FAIL\"}");
        JsonNode expected = objectMapper.readTree("{\"status\":\"OK\"}");
        when(jsonLoader.load("expected-output")).thenReturn(expected);

        assertThrows(AssertionError.class, () -> steps.aFilaDeveConter(QUEUE, "expected-output"));
    }
}
