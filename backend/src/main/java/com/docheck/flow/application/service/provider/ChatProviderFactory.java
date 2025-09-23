package com.docheck.flow.application.service.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ChatProviderFactory {

    private final ApplicationContext context;

    @Autowired
    public ChatProviderFactory(ApplicationContext context) {
        this.context = context;
    }

    public ChatProvider getProvider(String providerName) {
        return context.getBean(providerName, ChatProvider.class);
    }
}
