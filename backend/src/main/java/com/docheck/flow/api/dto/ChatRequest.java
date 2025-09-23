package com.docheck.flow.api.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String provider;
    private String model;
    private String prompt;
}
