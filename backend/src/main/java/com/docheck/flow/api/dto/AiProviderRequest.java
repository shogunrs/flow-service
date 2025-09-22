package com.docheck.flow.api.dto;

import lombok.Data;

@Data
public class AiProviderRequest {
    private String name;
    private String slug;
    private String baseUrl;
    private String docsUrl;
    private String notes;
    private Boolean enabled;
}