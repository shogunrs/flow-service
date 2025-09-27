package com.docheck.flow.api.dto;

import com.docheck.flow.domain.model.PromptAgent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptAgentDto {
    private String id;
    private String name;
    private String prompt;

    public static PromptAgentDto fromEntity(PromptAgent agent) {
        return new PromptAgentDto(agent.getId(), agent.getName(), agent.getPrompt());
    }
}
