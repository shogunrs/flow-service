package com.docheck.flow.infrastructure.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RAG_QUEUE_NAME = "rag.processing.queue";
    public static final String RAG_EXCHANGE_NAME = "rag.direct.exchange";
    public static final String RAG_ROUTING_KEY = "rag.routing.key";

    @Bean
    public Queue ragQueue() {
        return new Queue(RAG_QUEUE_NAME, true); // durable = true
    }

    @Bean
    public DirectExchange ragExchange() {
        return new DirectExchange(RAG_EXCHANGE_NAME);
    }

    @Bean
    public Binding ragBinding(Queue ragQueue, DirectExchange ragExchange) {
        return BindingBuilder.bind(ragQueue).to(ragExchange).with(RAG_ROUTING_KEY);
    }
}
