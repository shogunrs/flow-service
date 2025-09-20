package com.docheck.flow.api.mapper;

import com.docheck.flow.api.dto.CreateUserDTO;
import com.docheck.flow.api.dto.UserDTO;
import com.docheck.flow.application.service.command.UserWriteCommand;
import com.docheck.flow.domain.model.User;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserWriteCommand toCreateCommand(CreateUserDTO dto, String fallbackIp) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateUserDTO must not be null");
        }

        return UserWriteCommand.builder()
                .name(dto.getName())
                .email(Optional.ofNullable(dto.getEmail()).map(String::trim).orElse(null))
                .rawPassword(dto.getPassword())
                .roles(copyRoles(dto.getRoles()))
                .superUser(dto.getSuperUser())
                .cpf(dto.getCpf())
                .cnpj(dto.getCnpj())
                .rg(dto.getRg())
                .telefone(dto.getTelefone())
                .endereco(dto.getEndereco())
                .enderecoRua(dto.getEnderecoRua())
                .enderecoNumero(dto.getEnderecoNumero())
                .enderecoComplemento(dto.getEnderecoComplemento())
                .enderecoBairro(dto.getEnderecoBairro())
                .enderecoCidade(dto.getEnderecoCidade())
                .enderecoEstado(dto.getEnderecoEstado())
                .cep(dto.getCep())
                .banco(dto.getBanco())
                .agencia(dto.getAgencia())
                .conta(dto.getConta())
                .tipoConta(dto.getTipoConta())
                .pixTipo(dto.getPixTipo())
                .pixChave(dto.getPixChave())
                .profileImage(dto.getProfileImage())
                .fotoPerfilUrl(dto.getFotoPerfilUrl())
                .comprovanteEnderecoUrl(dto.getComprovanteEnderecoUrl())
                .documentoIdentidade(dto.getDocumentoIdentidade())
                .observacoes(dto.getObservacoes())
                .nomeCompleto(dto.getNomeCompleto())
                .quantidadeSocios(dto.getQuantidadeSocios())
                .enderecoEmpresa(dto.getEnderecoEmpresa())
                .enderecoEmpresaRua(dto.getEnderecoEmpresaRua())
                .enderecoEmpresaNumero(dto.getEnderecoEmpresaNumero())
                .enderecoEmpresaComplemento(dto.getEnderecoEmpresaComplemento())
                .enderecoEmpresaBairro(dto.getEnderecoEmpresaBairro())
                .enderecoEmpresaCidade(dto.getEnderecoEmpresaCidade())
                .enderecoEmpresaEstado(dto.getEnderecoEmpresaEstado())
                .cepEmpresa(dto.getCepEmpresa())
                .observacoesEmpresa(dto.getObservacoesEmpresa())
                .razaoSocial(dto.getRazaoSocial())
                .nomeFantasia(dto.getNomeFantasia())
                .cartaoCnpjImage(dto.getCartaoCnpjImage())
                .contratoSocialImage(dto.getContratoSocialImage())
                .qualificacaoSociosImage(dto.getQualificacaoSociosImage())
                .ipCadastro(Optional.ofNullable(dto.getIpCadastro()).filter(s -> !s.isBlank()).orElse(fallbackIp))
                .localizacaoCadastro(dto.getLocalizacaoCadastro())
                .build();
    }

    public static UserWriteCommand toUpdateCommand(String userId, UserDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("UserDTO must not be null");
        }
        return UserWriteCommand.builder()
                .id(userId)
                .name(dto.getName())
                .email(Optional.ofNullable(dto.getEmail()).map(String::trim).orElse(null))
                .roles(copyRoles(dto.getRoles()))
                .superUser(dto.getSuperUser())
                .cpf(dto.getCpf())
                .cnpj(dto.getCnpj())
                .rg(dto.getRg())
                .telefone(dto.getTelefone())
                .endereco(dto.getEndereco())
                .enderecoRua(dto.getEnderecoRua())
                .enderecoNumero(dto.getEnderecoNumero())
                .enderecoComplemento(dto.getEnderecoComplemento())
                .enderecoBairro(dto.getEnderecoBairro())
                .enderecoCidade(dto.getEnderecoCidade())
                .enderecoEstado(dto.getEnderecoEstado())
                .cep(dto.getCep())
                .banco(dto.getBanco())
                .agencia(dto.getAgencia())
                .conta(dto.getConta())
                .tipoConta(dto.getTipoConta())
                .pixTipo(dto.getPixTipo())
                .pixChave(dto.getPixChave())
                .profileImage(dto.getProfileImage())
                .fotoPerfilUrl(dto.getFotoPerfilUrl())
                .comprovanteEnderecoUrl(dto.getComprovanteEnderecoUrl())
                .documentoIdentidade(dto.getDocumentoIdentidade())
                .observacoes(dto.getObservacoes())
                .nomeCompleto(dto.getNomeCompleto())
                .quantidadeSocios(dto.getQuantidadeSocios())
                .enderecoEmpresa(dto.getEnderecoEmpresa())
                .enderecoEmpresaRua(dto.getEnderecoEmpresaRua())
                .enderecoEmpresaNumero(dto.getEnderecoEmpresaNumero())
                .enderecoEmpresaComplemento(dto.getEnderecoEmpresaComplemento())
                .enderecoEmpresaBairro(dto.getEnderecoEmpresaBairro())
                .enderecoEmpresaCidade(dto.getEnderecoEmpresaCidade())
                .enderecoEmpresaEstado(dto.getEnderecoEmpresaEstado())
                .cepEmpresa(dto.getCepEmpresa())
                .observacoesEmpresa(dto.getObservacoesEmpresa())
                .razaoSocial(dto.getRazaoSocial())
                .nomeFantasia(dto.getNomeFantasia())
                .cartaoCnpjImage(dto.getCartaoCnpjImage())
                .contratoSocialImage(dto.getContratoSocialImage())
                .qualificacaoSociosImage(dto.getQualificacaoSociosImage())
                .build();
    }

    public static UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .superUser(user.isSuperUser())
                .cpf(user.getCpf())
                .cnpj(user.getCnpj())
                .rg(user.getRg())
                .telefone(user.getTelefone())
                .endereco(user.getEndereco())
                .enderecoRua(user.getEnderecoRua())
                .enderecoNumero(user.getEnderecoNumero())
                .enderecoComplemento(user.getEnderecoComplemento())
                .enderecoBairro(user.getEnderecoBairro())
                .enderecoCidade(user.getEnderecoCidade())
                .enderecoEstado(user.getEnderecoEstado())
                .cep(user.getCep())
                .banco(user.getBanco())
                .agencia(user.getAgencia())
                .conta(user.getConta())
                .tipoConta(user.getTipoConta())
                .pixTipo(user.getPixTipo())
                .pixChave(user.getPixChave())
                .profileImage(user.getFotoPerfilUrl())
                .fotoPerfilUrl(user.getFotoPerfilUrl())
                .comprovanteEnderecoUrl(user.getComprovanteEnderecoUrl())
                .documentoIdentidade(user.getDocumentoIdentidade())
                .ultimoIpAcesso(user.getUltimoIpAcesso())
                .ultimaLocalizacao(user.getUltimaLocalizacao())
                .observacoes(user.getObservacoes())
                .nomeCompleto(user.getNomeCompleto())
                .quantidadeSocios(user.getQuantidadeSocios())
                .enderecoEmpresa(user.getEnderecoEmpresa())
                .enderecoEmpresaRua(user.getEnderecoEmpresaRua())
                .enderecoEmpresaNumero(user.getEnderecoEmpresaNumero())
                .enderecoEmpresaComplemento(user.getEnderecoEmpresaComplemento())
                .enderecoEmpresaBairro(user.getEnderecoEmpresaBairro())
                .enderecoEmpresaCidade(user.getEnderecoEmpresaCidade())
                .enderecoEmpresaEstado(user.getEnderecoEmpresaEstado())
                .cepEmpresa(user.getCepEmpresa())
                .observacoesEmpresa(user.getObservacoesEmpresa())
                .razaoSocial(user.getRazaoSocial())
                .nomeFantasia(user.getNomeFantasia())
                .cartaoCnpjImage(user.getCartaoCnpjImage())
                .contratoSocialImage(user.getContratoSocialImage())
                .qualificacaoSociosImage(user.getQualificacaoSociosImage())
                .ipCadastro(user.getIpCadastro())
                .localizacaoCadastro(user.getLocalizacaoCadastro())
                .build();
    }

    private static Set<String> copyRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(roles);
    }
}
