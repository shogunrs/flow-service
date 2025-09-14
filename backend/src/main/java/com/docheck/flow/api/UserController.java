package com.docheck.flow.api;

import com.docheck.flow.api.dto.CreateUserDTO;
import com.docheck.flow.api.dto.UserDTO;
import com.docheck.flow.application.service.UserService;
import com.docheck.flow.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public List<UserDTO> list() {
        return userService.list().stream().map(UserController::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable String id) {
        return userService.get(id).map(UserController::toDTO).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody CreateUserDTO dto) {
        Set<String> roles = dto.roles() == null ? java.util.Set.of() : dto.roles();
        User created = userService.create(dto.name(), dto.email(), dto.password(), roles);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.getId())).body(toDTO(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable String id, @RequestBody UserDTO dto) {
        User updated = userService.update(id, dto.name(), dto.roles());
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static UserDTO toDTO(User u) {
        return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRoles());
    }
}

