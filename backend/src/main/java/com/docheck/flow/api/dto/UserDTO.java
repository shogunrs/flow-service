package com.docheck.flow.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private Set<String> roles;
    private Boolean superUser;

    // Documentos pessoais
    private String cpf;
    private String cnpj;
    private String rg;
    private String telefone;

    // Endereço residencial
    private String endereco;
    private String enderecoRua;
    private String enderecoNumero;
    private String enderecoComplemento;
    private String enderecoBairro;
    private String enderecoCidade;
    private String enderecoEstado;
    private String cep;

    // Dados bancários
    private String banco;
    private String agencia;
    private String conta;
    private String tipoConta;

    // PIX
    private String pixTipo;
    private String pixChave;

    // Arquivos e biometria
    private String profileImage;
    private String fotoPerfilUrl;
    private String comprovanteEnderecoUrl;
    private String documentoIdentidade;
    private String ultimoIpAcesso;
    private String ultimaLocalizacao;

    // Dados pessoais adicionais
    private String observacoes;
    private String nomeCompleto;

    // Dados da empresa
    private Integer quantidadeSocios;
    private String enderecoEmpresa;
    private String enderecoEmpresaRua;
    private String enderecoEmpresaNumero;
    private String enderecoEmpresaComplemento;
    private String enderecoEmpresaBairro;
    private String enderecoEmpresaCidade;
    private String enderecoEmpresaEstado;
    private String cepEmpresa;
    private String observacoesEmpresa;
    private String razaoSocial;
    private String nomeFantasia;
    private String cartaoCnpjImage;
    private String contratoSocialImage;
    private String qualificacaoSociosImage;

    // Dados de auditoria
    private String ipCadastro;
    private String localizacaoCadastro;
}