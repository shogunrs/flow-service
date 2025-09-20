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
                            "user", UserDTO.builder()
                                    .id(u.getId())
                                    .name(u.getName())
                                    .email(u.getEmail())
                                    .roles(u.getRoles())
                                    .superUser(u.isSuperUser())
                                    .cpf(u.getCpf())
                                    .cnpj(u.getCnpj())
                                    .rg(u.getRg())
                                    .telefone(u.getTelefone())
                                    .endereco(u.getEndereco())
                                    .enderecoRua(u.getEnderecoRua())
                                    .enderecoNumero(u.getEnderecoNumero())
                                    .enderecoComplemento(u.getEnderecoComplemento())
                                    .enderecoBairro(u.getEnderecoBairro())
                                    .enderecoCidade(u.getEnderecoCidade())
                                    .enderecoEstado(u.getEnderecoEstado())
                                    .cep(u.getCep())
                                    .banco(u.getBanco())
                                    .agencia(u.getAgencia())
                                    .conta(u.getConta())
                                    .tipoConta(u.getTipoConta())
                                    .pixTipo(u.getPixTipo())
                                    .pixChave(u.getPixChave())
                                    .profileImage(u.getFotoPerfilUrl())
                                    .fotoPerfilUrl(u.getFotoPerfilUrl())
                                    .comprovanteEnderecoUrl(u.getComprovanteEnderecoUrl())
                                    .documentoIdentidade(u.getDocumentoIdentidade())
                                    .ultimoIpAcesso(u.getUltimoIpAcesso())
                                    .ultimaLocalizacao(u.getUltimaLocalizacao())
                                    .observacoes(u.getObservacoes())
                                    .nomeCompleto(u.getNomeCompleto())
                                    .quantidadeSocios(u.getQuantidadeSocios())
                                    .enderecoEmpresa(u.getEnderecoEmpresa())
                                    .enderecoEmpresaRua(u.getEnderecoEmpresaRua())
                                    .enderecoEmpresaNumero(u.getEnderecoEmpresaNumero())
                                    .enderecoEmpresaComplemento(u.getEnderecoEmpresaComplemento())
                                    .enderecoEmpresaBairro(u.getEnderecoEmpresaBairro())
                                    .enderecoEmpresaCidade(u.getEnderecoEmpresaCidade())
                                    .enderecoEmpresaEstado(u.getEnderecoEmpresaEstado())
                                    .cepEmpresa(u.getCepEmpresa())
                                    .observacoesEmpresa(u.getObservacoesEmpresa())
                                    .razaoSocial(u.getRazaoSocial())
                                    .nomeFantasia(u.getNomeFantasia())
                                    .cartaoCnpjImage(u.getCartaoCnpjImage())
                                    .contratoSocialImage(u.getContratoSocialImage())
                                    .qualificacaoSociosImage(u.getQualificacaoSociosImage())
                                    .ipCadastro(u.getIpCadastro())
                                    .localizacaoCadastro(u.getLocalizacaoCadastro())
                                    .build()
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

