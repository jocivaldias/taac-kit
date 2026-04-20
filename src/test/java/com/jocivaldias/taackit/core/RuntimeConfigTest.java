package com.jocivaldias.taackit.core;

import com.jocivaldias.taackit.http.HttpTestClient;
import com.jocivaldias.taackit.messaging.QueueClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RuntimeConfigTest {

    @Test
    void build_withBothClients_storesReferences() {
        HttpTestClient http = mock(HttpTestClient.class);
        QueueClient queue = mock(QueueClient.class);

        RuntimeConfig config = RuntimeConfig.builder()
            .httpTestClient(http)
            .queueClient(queue)
            .build();

        assertSame(http, config.getHttpTestClient());
        assertSame(queue, config.getQueueClient());
    }

    @Test
    void build_withOnlyHttpClient_queueClientIsNull() {
        HttpTestClient http = mock(HttpTestClient.class);

        RuntimeConfig config = RuntimeConfig.builder()
            .httpTestClient(http)
            .build();

        assertSame(http, config.getHttpTestClient());
        assertNull(config.getQueueClient());
    }

    @Test
    void build_withOnlyQueueClient_httpClientIsNull() {
        QueueClient queue = mock(QueueClient.class);

        RuntimeConfig config = RuntimeConfig.builder()
            .queueClient(queue)
            .build();

        assertNull(config.getHttpTestClient());
        assertSame(queue, config.getQueueClient());
    }

    @Test
    void build_withNoClients_succeeds() {
        RuntimeConfig config = RuntimeConfig.builder().build();

        assertNull(config.getHttpTestClient());
        assertNull(config.getQueueClient());
    }
}
