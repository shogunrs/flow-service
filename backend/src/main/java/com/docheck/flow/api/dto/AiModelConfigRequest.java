package com.docheck.flow.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AiModelConfigRequest {
    private String modelName;
    private String alias;
    private Double temperature;
    private Integer maxTokens;
    private Boolean defaultModel;
    private String description;
    private Map<String, Object> extras;
}