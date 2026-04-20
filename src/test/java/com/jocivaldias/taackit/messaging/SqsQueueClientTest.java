package com.jocivaldias.taackit.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SqsQueueClientTest {

    private static final String QUEUE_URL = "http://localhost:4566/000000000000/my-queue";
    private static final String QUEUE_NAME = "my-queue";

    private SqsClient sqsClient;
    private SqsQueueClient queueClient;

    @BeforeEach
    void setUp() {
        sqsClient = mock(SqsClient.class);
        queueClient = new SqsQueueClient(sqsClient);
    }

    @Test
    void sendMessage_withUrl_sendsDirectly() {
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder().build());

        queueClient.sendMessage(QUEUE_URL, "{\"key\":\"value\"}");

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        assertEquals(QUEUE_URL, captor.getValue().queueUrl());
        assertEquals("{\"key\":\"value\"}", captor.getValue().messageBody());
    }

    @Test
    void sendMessage_withQueueName_resolvesUrlFirst() {
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
            .thenReturn(GetQueueUrlResponse.builder().queueUrl(QUEUE_URL).build());
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder().build());

        queueClient.sendMessage(QUEUE_NAME, "payload");

        verify(sqsClient).getQueueUrl(any(GetQueueUrlRequest.class));
        verify(sqsClient).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void sendMessage_withAttributes_includesThemInRequest() {
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder().build());

        Map<String, MessageAttribute> attrs = Map.of(
            "eventType", new MessageAttribute("String", "USER_CREATED"));

        queueClient.sendMessage(QUEUE_URL, "payload", attrs);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        assertTrue(captor.getValue().messageAttributes().containsKey("eventType"));
        assertEquals("USER_CREATED",
            captor.getValue().messageAttributes().get("eventType").stringValue());
    }

    @Test
    void sendMessage_withNullAttributes_sendsWithoutAttributes() {
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder().build());

        queueClient.sendMessage(QUEUE_URL, "payload", null);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        assertTrue(captor.getValue().messageAttributes().isEmpty());
    }

    @Test
    void receiveMessage_withMessage_returnsBodyAndDeletes() {
        Message msg = Message.builder().body("{\"id\":1}").receiptHandle("rh-123").build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder().messages(msg).build());

        String result = queueClient.receiveMessage(QUEUE_URL);

        assertEquals("{\"id\":1}", result);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void receiveMessage_withNoMessage_returnsNull() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder().messages(Collections.emptyList()).build());

        String result = queueClient.receiveMessage(QUEUE_URL);

        assertNull(result);
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void constructor_withCustomWaitTime_usesItInReceive() {
        SqsQueueClient customClient = new SqsQueueClient(sqsClient, 10);
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder().messages(Collections.emptyList()).build());

        customClient.receiveMessage(QUEUE_URL);

        ArgumentCaptor<ReceiveMessageRequest> captor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        verify(sqsClient).receiveMessage(captor.capture());
        assertEquals(10, captor.getValue().waitTimeSeconds());
    }
}
