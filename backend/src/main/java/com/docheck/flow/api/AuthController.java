package com.docheck.flow.api;

import com.docheck.flow.api.dto.LoginDTO;
import com.docheck.flow.api.dto.UserDTO;
import com.docheck.flow.application.service.UserService;
import com.docheck.flow.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        return userService.findByEmail(dto.email())
                .filter(u -> userService.verifyPassword(u, dto.password()))
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "token", generateMockToken(u),
                        "user", new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRoles())
                )))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "invalid_credentials")));
    }

    private String generateMockToken(User u) {
        // Placeholder token. Replace with JWT in a future iteration.
        return u.getId() != null ? "tok_" + u.getId() : "tok_anonymous";
    }
}

