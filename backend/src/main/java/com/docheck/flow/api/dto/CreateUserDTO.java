package com.docheck.flow.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record CreateUserDTO(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        Set<String> roles,

        // Documentos (opcionais)
        String cpf,
        String cnpj,
        String rg,

        // Dados bancários (opcionais)
        String banco,
        String agencia,
        String conta,
        String tipoConta,

        // PIX (opcionais)
        String pixTipo,
        String pixChave,

        // Arquivos (opcionais)
        String fotoPerfilUrl,
        String comprovanteEnderecoUrl,

        // Dados de auditoria do cadastro
        String ipCadastro,
        String localizacaoCadastro
) {}

