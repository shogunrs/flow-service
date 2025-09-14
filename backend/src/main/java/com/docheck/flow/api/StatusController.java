package com.docheck.flow.api;

import com.docheck.flow.api.dto.StatusDTO;
import com.docheck.flow.application.service.StatusService;
import com.docheck.flow.domain.model.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/statuses")
public class StatusController {

    @Value("${app.statuses:Pendente,Em análise,Aprovado,Reprovado,Arquivado}")
    private String statuses;

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    // Main endpoint - returns simple list of names for compatibility with useStatuses.ts
    @GetMapping
    public ResponseEntity<List<String>> list() {
        // Try to get from database first
        try {
            List<Status> dbStatuses = statusService.findAll();
            if (!dbStatuses.isEmpty()) {
                List<String> names = dbStatuses.stream()
                        .map(Status::getName)
                        .toList();
                return ResponseEntity.ok(names);
            }
        } catch (Exception e) {
            // Fall back to config if database fails
        }

        // Fallback to configuration
        List<String> out = Arrays.stream(statuses.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    // Full objects with colors for admin panel
    @GetMapping("/detailed")
    public List<StatusDTO> listDetailed() {
        return statusService.findAll().stream()
                .map(StatusController::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatusDTO> getById(@PathVariable String id) {
        return statusService.findById(id)
                .map(status -> ResponseEntity.ok(toDto(status)))
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateStatusRequest(@NotBlank String name, @NotBlank String color) {}

    @PostMapping
    public ResponseEntity<StatusDTO> create(@Valid @RequestBody CreateStatusRequest request) {
        try {
            Status created = statusService.create(request.name(), request.color());
            return ResponseEntity.created(URI.create("/api/v1/statuses/" + created.getId()))
                    .body(toDto(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public record UpdateStatusRequest(@NotBlank String name, @NotBlank String color) {}

    @PutMapping("/{id}")
    public ResponseEntity<StatusDTO> update(@PathVariable String id,
                                           @Valid @RequestBody UpdateStatusRequest request) {
        try {
            Status updated = statusService.update(id, request.name(), request.color());
            return ResponseEntity.ok(toDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            statusService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private static StatusDTO toDto(Status status) {
        return new StatusDTO(
                status.getId(),
                status.getName(),
                status.getColor(),
                status.getCreatedAt(),
                status.getUpdatedAt()
        );
    }
}

