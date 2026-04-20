package com.jocivaldias.taackit.core;

import com.jocivaldias.taackit.http.HttpTestClient;
import com.jocivaldias.taackit.messaging.QueueClient;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class RuntimeConfig {

    private final HttpTestClient httpTestClient;
    private final QueueClient queueClient;
}
