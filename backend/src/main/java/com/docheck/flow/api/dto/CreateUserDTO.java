package com.docheck.flow.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record CreateUserDTO(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        Set<String> roles
) {}

