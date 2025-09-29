package com.docheck.flow.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalNotification {

    private String name;
    private String email;
    private String phone;
    private Boolean emailEnabled;
    private Boolean whatsappEnabled;

    public Boolean getEmailEnabled() {
        return emailEnabled != null ? emailEnabled : true;
    }

    public Boolean getWhatsappEnabled() {
        return whatsappEnabled != null ? whatsappEnabled : false;
    }
}