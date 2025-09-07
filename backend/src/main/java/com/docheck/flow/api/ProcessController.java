package com.docheck.flow.api;

import com.docheck.flow.api.dto.ProcessDTO;
import com.docheck.flow.api.mapper.ProcessMapper;
import com.docheck.flow.application.service.ProcessService;
import com.docheck.flow.domain.model.Process;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/processes")
public class ProcessController {
    private final ProcessService service;

    public ProcessController(ProcessService service) { this.service = service; }

    @GetMapping
    public List<ProcessDTO> list() {
        return service.list().stream().map(ProcessMapper::toDto).toList();
    }

    @GetMapping("/{key}")
    public ResponseEntity<ProcessDTO> get(@PathVariable("key") String externalId) {
        return service.get(externalId).map(p -> ResponseEntity.ok(ProcessMapper.toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateRequest(@NotBlank String key, @NotBlank String name) {}

    @PostMapping
    public ResponseEntity<ProcessDTO> create(@Valid @RequestBody CreateRequest req) {
        Process created = service.create(req.key(), req.name());
        return ResponseEntity.created(URI.create("/api/v1/processes/" + created.getExternalId()))
                .body(ProcessMapper.toDto(created));
    }

    public record UpdateNameRequest(@NotBlank String name) {}

    @PutMapping("/{key}")
    public ProcessDTO updateName(@PathVariable("key") String externalId, @Valid @RequestBody UpdateNameRequest req) {
        return ProcessMapper.toDto(service.updateName(externalId, req.name()));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable("key") String externalId) {
        service.delete(externalId);
        return ResponseEntity.noContent().build();
    }
}
