package com.docheck.flow.api.mapper;

import com.docheck.flow.api.dto.ProcessDTO;
import com.docheck.flow.domain.model.Process;

public class ProcessMapper {
    public static ProcessDTO toDto(Process p) {
        Process.ProcessType type = p.getType() != null ? p.getType() : Process.ProcessType.GENERIC;
        return new ProcessDTO(p.getExternalId(), p.getName(), p.isActive(),
                type.name(), type == Process.ProcessType.FINANCIAL);
    }
}
