package com.docheck.flow.infrastructure.outbox;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OutboxWorkerCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String mode = context.getEnvironment().getProperty("app.outbox.mode", "worker");
        return "worker".equalsIgnoreCase(mode);
    }
}

