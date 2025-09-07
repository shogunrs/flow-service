package com.docheck.flow.api.mapper;

import com.docheck.flow.api.dto.ProcessDTO;
import com.docheck.flow.domain.model.Process;

public class ProcessMapper {
    public static ProcessDTO toDto(Process p) {
        return new ProcessDTO(p.getKey(), p.getName(), p.isActive());
    }
}

