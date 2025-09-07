package com.docheck.flow.infrastructure.outbox;

import com.docheck.flow.application.port.EventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class OutboxEventPublisher implements EventPublisher {
    private final OutboxRepository repo;

    public OutboxEventPublisher(OutboxRepository repo) { this.repo = repo; }

    @Override
    public void publish(String type, Map<String, Object> payload) {
        OutboxDocument d = new OutboxDocument();
        d.type = type;
        d.payload = payload;
        d.createdAt = Instant.now();
        repo.save(d);
    }
}

