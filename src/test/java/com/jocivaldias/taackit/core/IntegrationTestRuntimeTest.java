package com.jocivaldias.taackit.core;

import com.jocivaldias.taackit.http.HttpTestClient;
import com.jocivaldias.taackit.messaging.QueueClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class IntegrationTestRuntimeTest {

    @BeforeEach
    void setUp() {
        IntegrationTestRuntime.reset();
    }

    @AfterEach
    void tearDown() {
        IntegrationTestRuntime.reset();
    }

    @Test
    void configure_withValidConfig_doesNotThrow() {
        RuntimeConfig config = RuntimeConfig.builder()
            .httpTestClient(mock(HttpTestClient.class))
            .build();

        assertDoesNotThrow(() -> IntegrationTestRuntime.configure(config));
    }

    @Test
    void configure_withNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> IntegrationTestRuntime.configure(null));
    }

    @Test
    void httpClient_beforeConfigure_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, IntegrationTestRuntime::httpClient);
    }

    @Test
    void queueClient_beforeConfigure_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, IntegrationTestRuntime::queueClient);
    }

    @Test
    void httpClient_whenConfigured_returnsClient() {
        HttpTestClient http = mock(HttpTestClient.class);
        IntegrationTestRuntime.configure(RuntimeConfig.builder().httpTestClient(http).build());

        assertSame(http, IntegrationTestRuntime.httpClient());
    }

    @Test
    void httpClient_whenNotSet_throwsIllegalStateException() {
        IntegrationTestRuntime.configure(RuntimeConfig.builder().build());

        assertThrows(IllegalStateException.class, IntegrationTestRuntime::httpClient);
    }

    @Test
    void queueClient_whenConfigured_returnsClient() {
        QueueClient queue = mock(QueueClient.class);
        IntegrationTestRuntime.configure(RuntimeConfig.builder().queueClient(queue).build());

        assertSame(queue, IntegrationTestRuntime.queueClient());
    }

    @Test
    void queueClient_whenNotSet_throwsIllegalStateException() {
        IntegrationTestRuntime.configure(RuntimeConfig.builder().build());

        assertThrows(IllegalStateException.class, IntegrationTestRuntime::queueClient);
    }

    @Test
    void configure_overridesPreviousConfig() {
        HttpTestClient first = mock(HttpTestClient.class);
        HttpTestClient second = mock(HttpTestClient.class);

        IntegrationTestRuntime.configure(RuntimeConfig.builder().httpTestClient(first).build());
        IntegrationTestRuntime.configure(RuntimeConfig.builder().httpTestClient(second).build());

        assertSame(second, IntegrationTestRuntime.httpClient());
    }
}
