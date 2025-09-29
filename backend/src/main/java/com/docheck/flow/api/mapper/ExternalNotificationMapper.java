package com.docheck.flow.api.mapper;

import com.docheck.flow.api.dto.ExternalNotificationDTO;
import com.docheck.flow.domain.model.ExternalNotification;

import java.util.List;
import java.util.stream.Collectors;

public final class ExternalNotificationMapper {
    private ExternalNotificationMapper() {}

    public static ExternalNotificationDTO toDto(ExternalNotification entity) {
        if (entity == null) {
            return null;
        }

        return ExternalNotificationDTO.builder()
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .emailEnabled(entity.getEmailEnabled())
                .whatsappEnabled(entity.getWhatsappEnabled())
                .build();
    }

    public static ExternalNotification toEntity(ExternalNotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        return ExternalNotification.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .emailEnabled(dto.getEmailEnabled())
                .whatsappEnabled(dto.getWhatsappEnabled())
                .build();
    }

    public static List<ExternalNotificationDTO> toDtoList(List<ExternalNotification> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(ExternalNotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<ExternalNotification> toEntityList(List<ExternalNotificationDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(ExternalNotificationMapper::toEntity)
                .collect(Collectors.toList());
    }
}