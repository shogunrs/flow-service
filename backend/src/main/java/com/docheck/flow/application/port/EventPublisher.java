package com.docheck.flow.application.port;

import java.util.Map;

public interface EventPublisher {
    void publish(String type, Map<String, Object> payload);
}

