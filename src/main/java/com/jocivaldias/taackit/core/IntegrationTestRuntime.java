package com.jocivaldias.taackit.core;

import com.jocivaldias.taackit.http.HttpTestClient;
import com.jocivaldias.taackit.messaging.QueueClient;

public final class IntegrationTestRuntime {

    private static volatile RuntimeConfig config;

    private IntegrationTestRuntime() {
    }

    public static synchronized void configure(RuntimeConfig runtimeConfig) {
        if (runtimeConfig == null) {
            throw new IllegalArgumentException("RuntimeConfig must not be null");
        }
        IntegrationTestRuntime.config = runtimeConfig;
    }

    public static HttpTestClient httpClient() {
        RuntimeConfig cfg = requireConfig();
        if (cfg.getHttpTestClient() == null) {
            throw new IllegalStateException(
                "httpTestClient not configured. Set it via RuntimeConfig.builder().httpTestClient(...)");
        }
        return cfg.getHttpTestClient();
    }

    public static QueueClient queueClient() {
        RuntimeConfig cfg = requireConfig();
        if (cfg.getQueueClient() == null) {
            throw new IllegalStateException(
                "queueClient not configured. Set it via RuntimeConfig.builder().queueClient(...)");
        }
        return cfg.getQueueClient();
    }

    private static RuntimeConfig requireConfig() {
        RuntimeConfig cfg = config;
        if (cfg == null) {
            throw new IllegalStateException(
                "IntegrationTestRuntime.configure() must be called before running tests");
        }
        return cfg;
    }

    static void reset() {
        config = null;
    }
}
