package com.docheck.flow.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChatRequest {
    private String provider;
    private String model;
    private String prompt;
    private MultipartFile file;
}
