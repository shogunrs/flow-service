package com.docheck.flow.api;

import com.docheck.flow.api.dto.LeadDTO;
import com.docheck.flow.api.mapper.LeadMapper;
import com.docheck.flow.application.service.LeadService;
import com.docheck.flow.application.service.command.LeadWriteCommand;
import com.docheck.flow.domain.model.Lead;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public List<LeadDTO> list() {
        return leadService.list().stream()
                .map(LeadMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody LeadRequest request) {
        try {
            LeadWriteCommand command = request.toCommand(null);
            Lead created = leadService.create(command);
            return ResponseEntity
                    .created(URI.create("/api/v1/leads/" + created.getId()))
                    .body(LeadMapper.toDto(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id, @Valid @RequestBody LeadRequest request) {
        try {
            LeadWriteCommand command = request.toCommand(id);
            Lead updated = leadService.update(command);
            return ResponseEntity.ok(LeadMapper.toDto(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        try {
            leadService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    public record LeadRequest(
            @NotBlank(message = "Nome é obrigatório") String name,
            @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
            String phone,
            String origin,
            String type
    ) {
        public LeadWriteCommand toCommand(String id) {
            return LeadWriteCommand.builder()
                    .id(id)
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .origin(origin)
                    .type(type)
                    .build();
        }
    }
}
