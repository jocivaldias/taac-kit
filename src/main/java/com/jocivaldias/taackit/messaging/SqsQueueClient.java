package com.jocivaldias.taackit.messaging;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;

public class SqsQueueClient implements QueueClient {

    private static final int DEFAULT_WAIT_TIME_SECONDS = 5;

    private final SqsClient sqsClient;
    private final int waitTimeSeconds;

    public SqsQueueClient(SqsClient sqsClient) {
        this(sqsClient, DEFAULT_WAIT_TIME_SECONDS);
    }

    public SqsQueueClient(SqsClient sqsClient, int waitTimeSeconds) {
        this.sqsClient = sqsClient;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    @Override
    public void sendMessage(String queueNameOrUrl, String payload) {
        sendMessage(queueNameOrUrl, payload, null);
    }

    @Override
    public void sendMessage(String queueNameOrUrl, String payload, Map<String, MessageAttribute> attributes) {
        String queueUrl = resolveQueueUrl(queueNameOrUrl);

        SendMessageRequest.Builder builder = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(payload);

        if (attributes != null && !attributes.isEmpty()) {
            Map<String, MessageAttributeValue> sqsAttrs = new HashMap<>();
            for (Map.Entry<String, MessageAttribute> entry : attributes.entrySet()) {
                MessageAttributeValue val = MessageAttributeValue.builder()
                    .dataType(entry.getValue().getDataType())
                    .stringValue(entry.getValue().getValue())
                    .build();
                sqsAttrs.put(entry.getKey(), val);
            }
            builder.messageAttributes(sqsAttrs);
        }

        sqsClient.sendMessage(builder.build());
    }

    @Override
    public String receiveMessage(String queueNameOrUrl) {
        String queueUrl = resolveQueueUrl(queueNameOrUrl);

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(1)
            .waitTimeSeconds(waitTimeSeconds)
            .messageAttributeNames("All")
            .build();

        ReceiveMessageResponse response = sqsClient.receiveMessage(request);
        if (response.hasMessages() && !response.messages().isEmpty()) {
            Message msg = response.messages().get(0);
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(msg.receiptHandle())
                .build());
            return msg.body();
        }
        return null;
    }

    private String resolveQueueUrl(String queueNameOrUrl) {
        if (queueNameOrUrl.startsWith("http://") || queueNameOrUrl.startsWith("https://")) {
            return queueNameOrUrl;
        }
        GetQueueUrlResponse resp = sqsClient.getQueueUrl(
            GetQueueUrlRequest.builder().queueName(queueNameOrUrl).build());
        return resp.queueUrl();
    }
}
