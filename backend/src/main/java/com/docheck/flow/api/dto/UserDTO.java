package com.docheck.flow.api.dto;

import java.util.Set;

public record UserDTO(
        String id,
        String name,
        String email,
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

        // Arquivos e biometria
        String fotoPerfilUrl,
        String comprovanteEnderecoUrl,
        String ultimoIpAcesso,
        String ultimaLocalizacao,

        // Dados de auditoria
        String ipCadastro,
        String localizacaoCadastro
) {}

