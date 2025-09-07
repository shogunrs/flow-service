package com.docheck.flow.api;

import com.docheck.flow.api.dto.StageDTO;
import com.docheck.flow.application.service.StageService;
import com.docheck.flow.domain.model.Stage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/processes/{processKey}/stages")
public class StageController {
    private final StageService service;
    public StageController(StageService service) { this.service = service; }

    @GetMapping
    public List<StageDTO> list(@PathVariable("processKey") String processKey) {
        return service.listByProcess(processKey).stream()
                .map(s -> new StageDTO(s.getId(), s.getTitle(), s.getSlaDays(), s.getColor(), s.getOrder()))
                .toList();
    }

    @PutMapping
    public ResponseEntity<List<StageDTO>> replace(@PathVariable("processKey") String processKey, @RequestBody List<StageDTO> body) {
        List<Stage> incoming = body.stream()
                .map(d -> {
                    Stage s = new Stage();
                    s.setTitle(d.title());
                    s.setSlaDays(d.slaDays());
                    s.setColor(d.color());
                    s.setOrder(d.order());
                    return s;
                }).toList();
        List<StageDTO> saved = service.replaceForProcess(processKey, incoming).stream()
                .map(s -> new StageDTO(s.getId(), s.getTitle(), s.getSlaDays(), s.getColor(), s.getOrder()))
                .toList();
        return ResponseEntity.ok(saved);
    }
}
