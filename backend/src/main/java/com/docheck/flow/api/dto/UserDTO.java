package com.docheck.flow.api.dto;

import java.util.Set;

public record UserDTO(
        String id,
        String name,
        String email,
        Set<String> roles
) {}

