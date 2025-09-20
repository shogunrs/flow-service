package com.docheck.flow.application.service.command;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * Aggregates all mutable user attributes used for creation and update flows.
 */
@Getter
@Builder(toBuilder = true)
public class UserWriteCommand {
    private final String id;
    private final String name;
    private final String email;
    private final String rawPassword;
    private final Set<String> roles;
    private final Boolean superUser;

    // Documentos pessoais
    private final String cpf;
    private final String cnpj;
    private final String rg;
    private final String telefone;

    // Endereço residencial
    private final String endereco;
    private final String enderecoRua;
    private final String enderecoNumero;
    private final String enderecoComplemento;
    private final String enderecoBairro;
    private final String enderecoCidade;
    private final String enderecoEstado;
    private final String cep;

    // Dados bancários
    private final String banco;
    private final String agencia;
    private final String conta;
    private final String tipoConta;

    // PIX
    private final String pixTipo;
    private final String pixChave;

    // Arquivos e biometria
    private final String profileImage;
    private final String fotoPerfilUrl;
    private final String comprovanteEnderecoUrl;
    private final String documentoIdentidade;

    // Dados pessoais adicionais
    private final String observacoes;
    private final String nomeCompleto;

    // Dados da empresa
    private final Integer quantidadeSocios;
    private final String enderecoEmpresa;
    private final String enderecoEmpresaRua;
    private final String enderecoEmpresaNumero;
    private final String enderecoEmpresaComplemento;
    private final String enderecoEmpresaBairro;
    private final String enderecoEmpresaCidade;
    private final String enderecoEmpresaEstado;
    private final String cepEmpresa;
    private final String observacoesEmpresa;
    private final String razaoSocial;
    private final String nomeFantasia;
    private final String cartaoCnpjImage;
    private final String contratoSocialImage;
    private final String qualificacaoSociosImage;

    // Dados de auditoria
    private final String ipCadastro;
    private final String localizacaoCadastro;
}
