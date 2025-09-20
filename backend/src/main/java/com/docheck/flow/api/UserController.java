package com.docheck.flow.api;

import com.docheck.flow.api.dto.CreateUserDTO;
import com.docheck.flow.api.dto.UserDTO;
import com.docheck.flow.api.mapper.UserMapper;
import com.docheck.flow.application.service.UserService;
import com.docheck.flow.application.service.command.UserWriteCommand;
import com.docheck.flow.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return userService.list().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable("id") String id) {
        return userService.get(id)
                .map(UserMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody CreateUserDTO dto, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        UserWriteCommand command = UserMapper.toCreateCommand(dto, clientIp);
        User created = userService.create(command);
        return ResponseEntity
                .created(URI.create("/api/v1/users/" + created.getId()))
                .body(UserMapper.toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable("id") String id, @Valid @RequestBody UserDTO dto) {
        UserWriteCommand command = UserMapper.toUpdateCommand(id, dto);
        User updated = userService.update(command);
        return ResponseEntity.ok(UserMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailable(
            @RequestParam(value = "email", required = false) String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Missing required query param 'email'",
                    "hint", "Call /api/v1/users/check-email?email=user@example.com"));
        }

        boolean available = userService.findByEmail(email).isEmpty();
        return ResponseEntity.ok(Map.of(
                "available", available,
                "email", email));
    }

    @PostMapping("/save-complete")
    public ResponseEntity<UserDTO> saveComplete(@RequestBody Map<String, Object> userData,
                                                HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);

        String name = stringValue(userData.get("name"));
        String email = stringValue(userData.get("email"));
        if (isBlank(name) || isBlank(email)) {
            return ResponseEntity.badRequest().build();
        }

        if (userService.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        String password = stringValue(userData.get("password"));
        if (isBlank(password)) {
            password = "temp123";
        }

        Set<String> roles = extractRoles(userData.get("roles"));
        Object superUserObj = userData.get("superUser");
        Boolean superUser = Boolean.FALSE;
        if (superUserObj instanceof Boolean) {
            superUser = (Boolean) superUserObj;
        }

        UserWriteCommand command = UserWriteCommand.builder()
                .name(name)
                .email(email)
                .rawPassword(password)
                .roles(roles)
                .superUser(superUser)
                .cpf(stringValue(userData.get("cpf")))
                .cnpj(stringValue(userData.get("cnpj")))
                .rg(stringValue(userData.get("rg")))
                .telefone(stringValue(userData.get("telefone")))
                .endereco(stringValue(userData.get("endereco")))
                .enderecoRua(stringValue(userData.get("enderecoRua")))
                .enderecoNumero(stringValue(userData.get("enderecoNumero")))
                .enderecoComplemento(stringValue(userData.get("enderecoComplemento")))
                .enderecoBairro(stringValue(userData.get("enderecoBairro")))
                .enderecoCidade(stringValue(userData.get("enderecoCidade")))
                .enderecoEstado(stringValue(userData.get("enderecoEstado")))
                .cep(stringValue(userData.get("cep")))
                .banco(stringValue(userData.get("banco")))
                .agencia(stringValue(userData.get("agencia")))
                .conta(stringValue(userData.get("conta")))
                .tipoConta(stringValue(userData.get("tipoConta")))
                .pixTipo(stringValue(userData.get("pixTipo")))
                .pixChave(stringValue(userData.get("pixChave")))
                .profileImage(stringValue(userData.get("profileImage")))
                .fotoPerfilUrl(stringValue(userData.get("fotoPerfilUrl")))
                .comprovanteEnderecoUrl(stringValue(userData.get("comprovanteEnderecoUrl")))
                .documentoIdentidade(stringValue(userData.get("documentoIdentidade")))
                .observacoes(stringValue(userData.get("observacoes")))
                .nomeCompleto(stringValue(userData.get("nomeCompleto")))
                .quantidadeSocios(integerValue(userData.get("quantidadeSocios")))
                .enderecoEmpresa(stringValue(userData.get("enderecoEmpresa")))
                .enderecoEmpresaRua(stringValue(userData.get("enderecoEmpresaRua")))
                .enderecoEmpresaNumero(stringValue(userData.get("enderecoEmpresaNumero")))
                .enderecoEmpresaComplemento(stringValue(userData.get("enderecoEmpresaComplemento")))
                .enderecoEmpresaBairro(stringValue(userData.get("enderecoEmpresaBairro")))
                .enderecoEmpresaCidade(stringValue(userData.get("enderecoEmpresaCidade")))
                .enderecoEmpresaEstado(stringValue(userData.get("enderecoEmpresaEstado")))
                .cepEmpresa(stringValue(userData.get("cepEmpresa")))
                .observacoesEmpresa(stringValue(userData.get("observacoesEmpresa")))
                .razaoSocial(stringValue(userData.get("razaoSocial")))
                .nomeFantasia(stringValue(userData.get("nomeFantasia")))
                .cartaoCnpjImage(stringValue(userData.get("cartaoCnpjImage")))
                .contratoSocialImage(stringValue(userData.get("contratoSocialImage")))
                .qualificacaoSociosImage(stringValue(userData.get("qualificacaoSociosImage")))
                .ipCadastro(clientIp)
                .localizacaoCadastro(stringValue(userData.get("localizacaoCadastro")))
                .build();

        User created = userService.create(command);
        return ResponseEntity
                .created(URI.create("/api/v1/users/" + created.getId()))
                .body(UserMapper.toDto(created));
    }

    @PostMapping("/create-basic")
    public ResponseEntity<UserDTO> createBasicUser(@RequestBody Map<String, String> basicData,
                                                   HttpServletRequest request) {
        String name = basicData.get("name");
        String email = basicData.get("email");
        if (isBlank(name) || isBlank(email)) {
            return ResponseEntity.badRequest().build();
        }

        if (userService.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        String password = basicData.get("password");
        if (isBlank(password)) {
            password = "temp123";
        }

        String clientIp = getClientIpAddress(request);

        UserWriteCommand command = UserWriteCommand.builder()
                .name(name)
                .email(email)
                .rawPassword(password)
                .roles(Set.of("user"))
                .superUser(Boolean.FALSE)
                .ipCadastro(clientIp)
                .build();

        User created = userService.create(command);
        return ResponseEntity
                .created(URI.create("/api/v1/users/" + created.getId()))
                .body(UserMapper.toDto(created));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (isBlank(xForwardedForHeader)) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0].trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Set<String> extractRoles(Object roles) {
        if (roles instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(role -> !role.isBlank())
                    .collect(Collectors.toSet());
        }
        if (roles instanceof Set<?> set) {
            return set.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(role -> !role.isBlank())
                    .collect(Collectors.toSet());
        }
        return new HashSet<>(Collections.singleton("user"));
    }
}
