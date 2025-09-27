package com.docheck.flow.application.service;

import com.docheck.flow.application.port.LeadRepository;
import com.docheck.flow.application.port.UserRepository;
import com.docheck.flow.application.service.command.LeadWriteCommand;
import com.docheck.flow.domain.model.Lead;
import com.docheck.flow.domain.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final StatusService statusService;
    private volatile boolean leadStatusesEnsured = false;

    public LeadService(LeadRepository leadRepository, UserRepository userRepository, StatusService statusService) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.statusService = statusService;
    }

    public List<Lead> list() {
        ensureLeadStatuses();
        User actor = ensureLeadPermissions();
        return leadRepository.findAllByOrganizationId(actor.getOrganizationId());
    }

    public Lead create(LeadWriteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Lead command must not be null");
        }
        ensureLeadStatuses();
        User actor = ensureLeadPermissions();

        String name = normalize(command.getName());
        String email = normalizeEmail(command.getEmail());
        String phone = normalize(command.getPhone());
        Lead.LeadOrigin origin = Lead.LeadOrigin.fromString(command.getOrigin());
        Lead.LeadType type = Lead.LeadType.fromString(command.getType());

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (leadRepository.existsByEmailAndOrganizationId(email, actor.getOrganizationId())) {
            throw new IllegalArgumentException("Já existe um lead com este email");
        }

        Instant now = Instant.now();
        Lead lead = Lead.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .origin(origin)
                .type(type)
                .organizationId(actor.getOrganizationId())
                .createdBy(actor.getId())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return leadRepository.save(lead);
    }

    public Lead update(LeadWriteCommand command) {
        if (command == null || command.getId() == null) {
            throw new IllegalArgumentException("Lead command must contain an id");
        }
        ensureLeadStatuses();
        User actor = ensureLeadPermissions();

        Lead existing = leadRepository.findByIdAndOrganizationId(command.getId(), actor.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado"));

        String name = normalize(command.getName());
        String email = normalizeEmail(command.getEmail());
        String phone = normalize(command.getPhone());
        Lead.LeadOrigin origin = Lead.LeadOrigin.fromString(command.getOrigin());
        Lead.LeadType type = Lead.LeadType.fromString(command.getType());

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (existing.getEmail() != null && !existing.getEmail().equalsIgnoreCase(email) &&
                leadRepository.existsByEmailAndOrganizationId(email, actor.getOrganizationId())) {
            throw new IllegalArgumentException("Já existe um lead com este email");
        }

        existing.setName(name);
        existing.setEmail(email);
        existing.setPhone(phone);
        existing.setOrigin(origin);
        existing.setType(type);
        existing.setUpdatedAt(Instant.now());

        return leadRepository.save(existing);
    }

    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id do lead é obrigatório");
        }
        ensureLeadStatuses();
        User actor = ensureLeadPermissions();
        leadRepository.deleteByIdAndOrganizationId(id, actor.getOrganizationId());
    }

    private User ensureLeadPermissions() {
        User actor = getAuthenticatedUser();
        if (actor.isSuperUser()) {
            return actor;
        }
        if (actor.getRoles() != null) {
            boolean allowed = actor.getRoles().stream()
                    .filter(role -> role != null)
                    .map(role -> role.trim().toLowerCase())
                    .anyMatch(role -> role.equals("admin") || role.equals("manager"));
            if (allowed) {
                return actor;
            }
        }
        throw new IllegalStateException("Você não possui permissão para gerenciar leads");
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return userRepository.findAny()
                    .orElseThrow(() -> new IllegalStateException("Nenhum usuário autenticado encontrado"));
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        return email.trim().toLowerCase();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private void ensureLeadStatuses() {
        if (leadStatusesEnsured) {
            return;
        }
        synchronized (this) {
            if (leadStatusesEnsured) {
                return;
            }
            statusService.ensureLeadStatuses();
            leadStatusesEnsured = true;
        }
    }
}
