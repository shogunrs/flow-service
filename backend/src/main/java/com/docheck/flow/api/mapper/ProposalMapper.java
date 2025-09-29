package com.docheck.flow.api.mapper;

import com.docheck.flow.api.dto.ProposalDTO;
import com.docheck.flow.api.dto.UpdateAccessControlDTO;
import com.docheck.flow.domain.model.Proposal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProposalMapper {
    private ProposalMapper() {}

    public static ProposalDTO toDto(Proposal proposal) {
        if (proposal == null) {
            return null;
        }

        return new ProposalDTO(
                proposal.getId(),
                proposal.getName(),
                proposal.getAmount(),
                proposal.getStageId(),
                proposal.getStatus(),
                proposal.isArchived(),
                proposal.getStageEnteredAt(),
                proposal.getCreatedAt(),
                proposal.getUpdatedAt(),
                proposal.getDetails(),
                // Campos de controle de acesso
                proposal.getCreatedBy(),
                proposal.getOrganizationId(),
                proposal.getIsPublic(),
                proposal.getVisibleToUsers(),
                ExternalNotificationMapper.toDtoList(proposal.getExternalNotifications())
        );
    }

    public static Proposal toEntity(ProposalDTO dto) {
        if (dto == null) {
            return null;
        }

        Proposal proposal = new Proposal();
        proposal.setId(dto.id());
        proposal.setName(dto.name());
        proposal.setAmount(dto.amount());
        proposal.setStageId(dto.stageId());
        proposal.setStatus(dto.status());
        proposal.setArchived(dto.archived() != null ? dto.archived() : false);
        proposal.setStageEnteredAt(dto.stageEnteredAt());
        proposal.setCreatedAt(dto.createdAt());
        proposal.setUpdatedAt(dto.updatedAt());
        proposal.setDetails(dto.details());

        // Campos de controle de acesso
        proposal.setCreatedBy(dto.createdBy());
        proposal.setOrganizationId(dto.organizationId());
        proposal.setIsPublic(dto.isPublic());
        proposal.setVisibleToUsers(dto.visibleToUsers() != null ? dto.visibleToUsers() : Set.of());
        proposal.setExternalNotifications(ExternalNotificationMapper.toEntityList(dto.externalNotifications()));

        return proposal;
    }

    public static void updateAccessControl(Proposal proposal, UpdateAccessControlDTO updateDto) {
        if (proposal == null || updateDto == null) {
            return;
        }

        proposal.setIsPublic(updateDto.getIsPublic());

        if (updateDto.getVisibleToUsers() != null) {
            proposal.setVisibleToUsers(new HashSet<>(updateDto.getVisibleToUsers()));
        }

        if (updateDto.getExternalNotifications() != null) {
            proposal.setExternalNotifications(
                ExternalNotificationMapper.toEntityList(updateDto.getExternalNotifications())
            );
        }
    }
}