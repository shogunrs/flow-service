package com.docheck.flow.api;

import com.docheck.flow.api.dto.StageFieldDTO;
import com.docheck.flow.application.service.StageFieldService;
import com.docheck.flow.domain.model.StageField;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stages/{stageId}/fields")
public class StageFieldController {
    private final StageFieldService service;
    public StageFieldController(StageFieldService service) { this.service = service; }

    @GetMapping
    public List<StageFieldDTO> list(@PathVariable("stageId") String stageId) {
        return service.listByStage(stageId).stream()
                .map(f -> new StageFieldDTO(f.getId(), f.getStageId(), f.getLabel(), f.getType(), f.isRequired(), f.getPlaceholder(), f.getOptions(), f.getOrder()))
                .toList();
    }

    @PutMapping
    public ResponseEntity<List<StageFieldDTO>> replace(@PathVariable("stageId") String stageId,
                                                       @RequestBody List<StageFieldDTO> body) {
        List<StageField> incoming = body.stream().map(d -> {
            StageField f = new StageField();
            f.setLabel(d.label());
            f.setType(d.type());
            f.setRequired(Boolean.TRUE.equals(d.required()));
            f.setPlaceholder(d.placeholder());
            f.setOptions(d.options());
            f.setOrder(d.order());
            return f;
        }).toList();
        List<StageFieldDTO> saved = service.replaceForStage(stageId, incoming).stream()
                .map(f -> new StageFieldDTO(f.getId(), f.getStageId(), f.getLabel(), f.getType(), f.isRequired(), f.getPlaceholder(), f.getOptions(), f.getOrder()))
                .toList();
        return ResponseEntity.ok(saved);
    }
}

