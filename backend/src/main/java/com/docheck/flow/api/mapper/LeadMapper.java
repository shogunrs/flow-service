package com.docheck.flow.api.mapper;

import com.docheck.flow.api.dto.LeadDTO;
import com.docheck.flow.application.service.command.LeadWriteCommand;
import com.docheck.flow.domain.model.Lead;

public final class LeadMapper {
    private LeadMapper() {
    }

    public static LeadDTO toDto(Lead lead) {
        if (lead == null) {
            return null;
        }
        return LeadDTO.builder()
                .id(lead.getId())
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .origin(lead.getOrigin())
                .type(lead.getType())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }

    public static LeadWriteCommand toCommand(LeadDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("LeadDTO não pode ser nulo");
        }
        return LeadWriteCommand.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .origin(dto.getOrigin() != null ? dto.getOrigin().name() : null)
                .type(dto.getType() != null ? dto.getType().name() : null)
                .build();
    }
}
