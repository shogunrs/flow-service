package com.docheck.flow.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {
    private String id;
    private String organizationId;
    private String createdBy;
    private String name;
    private String email;
    private String phone;
    private LeadOrigin origin;
    private LeadType type;
    private Instant createdAt;
    private Instant updatedAt;

    public enum LeadType {
        CLIENT,
        ANALYST_CANDIDATE;

        public static LeadType fromString(String value) {
            if (value == null) {
                return CLIENT;
            }
            try {
                return LeadType.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return CLIENT;
            }
        }
    }

    public enum LeadOrigin {
        INDICACAO,
        EVENTO,
        SITE,
        ANUNCIO,
        SOCIAL_MEDIA,
        PARCERIA,
        COLD_CALL,
        EMAIL_MARKETING,
        RETORNO_CLIENTE,
        OUTROS;

        public static LeadOrigin fromString(String value) {
            if (value == null) {
                return INDICACAO;
            }
            try {
                return LeadOrigin.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return INDICACAO;
            }
        }
    }
}
