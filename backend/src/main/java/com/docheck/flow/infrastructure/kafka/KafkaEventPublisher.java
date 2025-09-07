package com.docheck.flow.infrastructure.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class KafkaEventPublisher {
    private final KafkaTemplate<String, String> kafka;
    private final String topic;
    private final ObjectMapper mapper = new ObjectMapper();

    public KafkaEventPublisher(KafkaTemplate<String, String> kafka, @Value("${app.kafka.events-topic}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    public void publish(String type, Map<String, Object> payload) {
        try {
            payload.putIfAbsent("type", type);
            String value = mapper.writeValueAsString(payload);
            kafka.send(topic, value);
        } catch (Exception e) {
            // Em produção, registrar para retry/monitoramento
        }
    }
}
