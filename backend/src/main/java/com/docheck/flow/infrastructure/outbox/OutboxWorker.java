package com.docheck.flow.infrastructure.outbox;

import com.docheck.flow.infrastructure.kafka.KafkaEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Conditional(OutboxWorkerCondition.class)
public class OutboxWorker {
    private final OutboxRepository repo;
    private final KafkaEventPublisher kafka;

    public OutboxWorker(OutboxRepository repo, KafkaEventPublisher kafka) {
        this.repo = repo;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    public void drain() {
        var batch = repo.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (var d : batch) {
            try {
                kafka.publish(d.type, d.payload);
                d.publishedAt = Instant.now();
                repo.save(d);
            } catch (Exception ignored) { }
        }
    }
}

