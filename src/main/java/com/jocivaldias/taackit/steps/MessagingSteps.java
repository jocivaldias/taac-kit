package com.jocivaldias.taackit.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jocivaldias.taackit.core.IntegrationTestRuntime;
import com.jocivaldias.taackit.json.ClasspathJsonResourceLoader;
import com.jocivaldias.taackit.json.JsonComparator;
import com.jocivaldias.taackit.json.JsonComparatorRegistry;
import com.jocivaldias.taackit.json.JsonResourceLoader;
import com.jocivaldias.taackit.messaging.MessageAttribute;
import com.jocivaldias.taackit.support.DurationUtils;
import com.jocivaldias.taackit.support.ScenarioContext;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessagingSteps {

    private final JsonResourceLoader jsonLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessagingSteps() {
        this.jsonLoader = new ClasspathJsonResourceLoader("payloads");
    }

    MessagingSteps(JsonResourceLoader jsonLoader) {
        this.jsonLoader = jsonLoader;
    }

    @Quando("envia mensagem na fila {string} com {string}")
    public void enviaMensagemNaFila(String queue, String payloadId) {
        JsonNode envelope = jsonLoader.load(payloadId);
        JsonNode bodyNode = envelope.has("body") ? envelope.get("body") : envelope;
        Map<String, MessageAttribute> attributes = extractMessageAttributes(envelope.get("attributes"));

        try {
            IntegrationTestRuntime.queueClient()
                .sendMessage(queue, objectMapper.writeValueAsString(bodyNode), attributes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to queue: " + queue, e);
        }
    }

    @E("aguarda {string}")
    public void aguarda(String delayStr) {
        Duration delay = DurationUtils.parse(delayStr);
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting", e);
        }
    }

    @Entao("a fila {string} deve conter {string}")
    public void aFilaDeveConter(String queue, String payloadId) {
        String body = IntegrationTestRuntime.queueClient().receiveMessage(queue);
        if (body == null) {
            throw new AssertionError("Nenhuma mensagem encontrada na fila: " + queue);
        }

        JsonNode actual;
        try {
            actual = objectMapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON from queue message", e);
        }

        JsonNode expected = jsonLoader.load(payloadId);
        JsonComparator comparator = JsonComparatorRegistry.get(
            ScenarioContext.current().getJsonOptions().getComparatorName());
        comparator.assertEquals(expected, actual, ScenarioContext.current().getJsonOptions());
    }

    private Map<String, MessageAttribute> extractMessageAttributes(JsonNode attributesNode) {
        Map<String, MessageAttribute> result = new HashMap<>();
        if (attributesNode == null || attributesNode.isMissingNode() || !attributesNode.isObject()) {
            return result;
        }
        Iterator<String> fieldNames = attributesNode.fieldNames();
        while (fieldNames.hasNext()) {
            String attrName = fieldNames.next();
            JsonNode attrNode = attributesNode.get(attrName);
            if (attrNode == null || !attrNode.isObject()) continue;

            String type = attrNode.has("type") && attrNode.get("type").isTextual()
                ? attrNode.get("type").asText() : "String";
            JsonNode valueNode = attrNode.get("value");
            if (valueNode != null && !valueNode.isNull()) {
                result.put(attrName, new MessageAttribute(type, valueNode.asText()));
            }
        }
        return result;
    }
}
