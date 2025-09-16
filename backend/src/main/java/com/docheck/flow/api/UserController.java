package com.docheck.flow.api;

import com.docheck.flow.api.dto.CreateUserDTO;
import com.docheck.flow.api.dto.UserDTO;
import com.docheck.flow.application.service.UserService;
import com.docheck.flow.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> list() {
        return userService.list().stream().map(UserController::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable(value = "id") String id) {
        return userService.get(id).map(UserController::toDTO).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody CreateUserDTO dto, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        Set<String> roles = dto.roles() == null ? java.util.Set.of() : dto.roles();
        User created = userService.create(dto.name(), dto.email(), dto.password(), roles,
                dto.cpf(), dto.cnpj(), dto.rg(),
                dto.banco(), dto.agencia(), dto.conta(), dto.tipoConta(),
                dto.pixTipo(), dto.pixChave(),
                dto.fotoPerfilUrl(), dto.comprovanteEnderecoUrl(),
                clientIp, dto.localizacaoCadastro());
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.getId())).body(toDTO(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable(value = "id") String id, @RequestBody UserDTO dto) {
        User updated = userService.update(id, dto.name(), dto.email(), dto.roles(),
                dto.cpf(), dto.cnpj(), dto.rg(),
                dto.banco(), dto.agencia(), dto.conta(), dto.tipoConta(),
                dto.pixTipo(), dto.pixChave());
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailable(@RequestParam(value = "email", required = false) String email) {
        System.out.println("Received email check request for: " + email);
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error", "Missing required query param 'email'",
                    "hint", "Call /api/v1/users/check-email?email=user@example.com"));
        }

        System.out.println("Checking availability for email: " + email);
        boolean isAvailable = userService.findByEmail(email).isEmpty();
        return ResponseEntity.ok(java.util.Map.of(
                "available", isAvailable,
                "email", email));
    }

    @PostMapping("/create-basic")
    public ResponseEntity<UserDTO> createBasicUser(@RequestBody java.util.Map<String, String> basicData,
            HttpServletRequest request) {
        String name = basicData.get("name");
        String email = basicData.get("email");
        String password = basicData.get("password");
        String clientIp = getClientIpAddress(request);

        // Verificar se email já existe
        if (userService.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // Criar usuário básico com senha padrão se não fornecida
        if (password == null || password.isEmpty()) {
            password = "temp123"; // Senha temporária que deve ser alterada
        }

        User created = userService.create(name, email, password, java.util.Set.of("user"),
                null, null, null, // documentos vazios
                null, null, null, null, // dados bancários vazios
                null, null, // PIX vazio
                null, null, // arquivos vazios
                clientIp, null); // localização vazia por enquanto

        return ResponseEntity.created(URI.create("/api/v1/users/" + created.getId())).body(toDTO(created));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    private static UserDTO toDTO(User u) {
        return new UserDTO(
                u.getId(), u.getName(), u.getEmail(), u.getRoles(),
                u.getCpf(), u.getCnpj(), u.getRg(),
                u.getBanco(), u.getAgencia(), u.getConta(), u.getTipoConta(),
                u.getPixTipo(), u.getPixChave(),
                u.getFotoPerfilUrl(), u.getComprovanteEnderecoUrl(),
                u.getUltimoIpAcesso(), u.getUltimaLocalizacao(),
                u.getIpCadastro(), u.getLocalizacaoCadastro());
    }
}
