package com.docheck.flow.application.service.provider;

import com.docheck.flow.api.dto.ChatRequest;
import com.docheck.flow.api.dto.ChatResponse;
import com.docheck.flow.domain.model.AiProvider;

public interface ChatProvider {
    ChatResponse getChatResponse(ChatRequest request, AiProvider provider);
}
