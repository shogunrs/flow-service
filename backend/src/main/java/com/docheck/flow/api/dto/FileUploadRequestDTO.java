package com.docheck.flow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileUploadRequestDTO(
    @NotBlank(message = "Filename is required")
    String filename,

    @NotBlank(message = "Content type is required")
    String contentType,

    @NotNull(message = "File type is required")
    String fileType
) {}