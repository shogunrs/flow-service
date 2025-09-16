package com.docheck.flow.api;

import com.docheck.flow.api.dto.LoginDTO;
import com.docheck.flow.api.dto.UserDTO;
import com.docheck.flow.application.service.UserService;
import com.docheck.flow.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);

        return userService.findByEmail(dto.email())
                .filter(u -> userService.verifyPassword(u, dto.password()))
                .<ResponseEntity<?>>map(u -> {
                    // Atualizar informações de último acesso
                    userService.updateAccessInfo(u.getId(), clientIp, null);

                    return ResponseEntity.ok(Map.of(
                            "token", generateMockToken(u),
                            "user", new UserDTO(
                                    u.getId(), u.getName(), u.getEmail(), u.getRoles(),
                                    u.getCpf(), u.getCnpj(), u.getRg(),
                                    u.getBanco(), u.getAgencia(), u.getConta(), u.getTipoConta(),
                                    u.getPixTipo(), u.getPixChave(),
                                    u.getFotoPerfilUrl(), u.getComprovanteEnderecoUrl(),
                                    u.getUltimoIpAcesso(), u.getUltimaLocalizacao(),
                                    u.getIpCadastro(), u.getLocalizacaoCadastro()
                            )
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "invalid_credentials")));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    private String generateMockToken(User u) {
        // Placeholder token. Replace with JWT in a future iteration.
        return u.getId() != null ? "tok_" + u.getId() : "tok_anonymous";
    }
}

