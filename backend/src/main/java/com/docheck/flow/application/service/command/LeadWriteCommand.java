package com.docheck.flow.application.service.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeadWriteCommand {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private final String origin;
    private final String type;
}
