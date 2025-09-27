package com.docheck.flow.api.dto;

import lombok.Data;

@Data
public class CreateOrUpdatePromptAgentRequest {
    private String name;
    private String prompt;
}
