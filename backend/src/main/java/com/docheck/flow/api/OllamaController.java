package com.docheck.flow.api;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ollama")
@RequiredArgsConstructor
public class OllamaController {

    private final ChatClient.Builder chatClientBuilder;

    @GetMapping("/ask")
    public String ask(@RequestParam String q,
            @RequestParam(name = "model", defaultValue = "llama3.1:8b") String model) {
        ChatOptions chatOptions = ChatOptions.builder().model(model).build();
        return chatClientBuilder
                .build()
                .prompt()
                .options(chatOptions)
                .user(q)
                .call()
                .content();
    }
}