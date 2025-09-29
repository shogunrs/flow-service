package com.docheck.flow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccessControlDTO {
    private Boolean isPublic;
    private Set<String> visibleToUsers;
    private List<ExternalNotificationDTO> externalNotifications;

    public Boolean getIsPublic() {
        return isPublic != null ? isPublic : true;
    }
}