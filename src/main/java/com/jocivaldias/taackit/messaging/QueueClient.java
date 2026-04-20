package com.jocivaldias.taackit.messaging;

import java.util.Map;

public interface QueueClient {

    void sendMessage(String queueNameOrUrl, String payload);

    void sendMessage(String queueNameOrUrl, String payload, Map<String, MessageAttribute> attributes);

    String receiveMessage(String queueNameOrUrl);
}
