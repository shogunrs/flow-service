package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.UserRepository;
import com.docheck.flow.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MongoUserRepository implements UserRepository {
    private final SpringDataUserRepository repo;

    public MongoUserRepository(SpringDataUserRepository repo) {
        this.repo = repo;
    }

    private static User toDomain(UserDocument doc) {
        if (doc == null) {
            return null;
        }

        return User.builder()
                .id(doc.getId())
                .name(doc.getName())
                .email(doc.getEmail())
                .passwordHash(doc.getPasswordHash())
                .roles(copyRoles(doc.getRoles()))
                .superUser(doc.isSuperUser())
                .organizationId(doc.getOrganizationId())
                .createdBy(doc.getCreatedBy())
                .cpf(doc.getCpf())
                .cnpj(doc.getCnpj())
                .rg(doc.getRg())
                .telefone(doc.getTelefone())
                .endereco(doc.getEndereco())
                .enderecoRua(doc.getEnderecoRua())
                .enderecoNumero(doc.getEnderecoNumero())
                .enderecoComplemento(doc.getEnderecoComplemento())
                .enderecoBairro(doc.getEnderecoBairro())
                .enderecoCidade(doc.getEnderecoCidade())
                .enderecoEstado(doc.getEnderecoEstado())
                .cep(doc.getCep())
                .banco(doc.getBanco())
                .agencia(doc.getAgencia())
                .conta(doc.getConta())
                .tipoConta(doc.getTipoConta())
                .pixTipo(doc.getPixTipo())
                .pixChave(doc.getPixChave())
                .fotoPerfilUrl(doc.getFotoPerfilUrl())
                .comprovanteEnderecoUrl(doc.getComprovanteEnderecoUrl())
                .documentoIdentidade(doc.getDocumentoIdentidade())
                .faceEmbedding(doc.getFaceEmbedding())
                .ultimoIpAcesso(doc.getUltimoIpAcesso())
                .ultimaLocalizacao(doc.getUltimaLocalizacao())
                .observacoes(doc.getObservacoes())
                .nomeCompleto(doc.getNomeCompleto())
                .quantidadeSocios(doc.getQuantidadeSocios())
                .enderecoEmpresa(doc.getEnderecoEmpresa())
                .enderecoEmpresaRua(doc.getEnderecoEmpresaRua())
                .enderecoEmpresaNumero(doc.getEnderecoEmpresaNumero())
                .enderecoEmpresaComplemento(doc.getEnderecoEmpresaComplemento())
                .enderecoEmpresaBairro(doc.getEnderecoEmpresaBairro())
                .enderecoEmpresaCidade(doc.getEnderecoEmpresaCidade())
                .enderecoEmpresaEstado(doc.getEnderecoEmpresaEstado())
                .cepEmpresa(doc.getCepEmpresa())
                .observacoesEmpresa(doc.getObservacoesEmpresa())
                .razaoSocial(doc.getRazaoSocial())
                .nomeFantasia(doc.getNomeFantasia())
                .cartaoCnpjImage(doc.getCartaoCnpjImage())
                .contratoSocialImage(doc.getContratoSocialImage())
                .qualificacaoSociosImage(doc.getQualificacaoSociosImage())
                .ipCadastro(doc.getIpCadastro())
                .localizacaoCadastro(doc.getLocalizacaoCadastro())
                .tags(copyStrings(doc.getTags()))
                .fotoPerfilReference(normalizeReference(doc.getFotoPerfilReference()))
                .comprovanteEnderecoReference(normalizeReference(doc.getComprovanteEnderecoReference()))
                .documentoIdentidadeReference(normalizeReference(doc.getDocumentoIdentidadeReference()))
                .cartaoCnpjReference(normalizeReference(doc.getCartaoCnpjReference()))
                .contratoSocialReference(normalizeReference(doc.getContratoSocialReference()))
                .qualificacaoSociosReference(normalizeReference(doc.getQualificacaoSociosReference()))
                .outrosArquivos(copyFileReferences(doc.getOutrosArquivos()))
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private static UserDocument toDoc(User user) {
        if (user == null) {
            return null;
        }

        return UserDocument.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .roles(copyRoles(user.getRoles()))
                .superUser(user.isSuperUser())
                .organizationId(user.getOrganizationId())
                .createdBy(user.getCreatedBy())
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
                .fotoPerfilUrl(user.getFotoPerfilUrl())
                .comprovanteEnderecoUrl(user.getComprovanteEnderecoUrl())
                .documentoIdentidade(user.getDocumentoIdentidade())
                .faceEmbedding(user.getFaceEmbedding())
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
                .cartaoCnpjReference(user.getCartaoCnpjReference())
                .contratoSocialReference(user.getContratoSocialReference())
                .qualificacaoSociosReference(user.getQualificacaoSociosReference())
                .ipCadastro(user.getIpCadastro())
                .localizacaoCadastro(user.getLocalizacaoCadastro())
                .tags(copyStrings(user.getTags()))
                .fotoPerfilReference(user.getFotoPerfilReference())
                .comprovanteEnderecoReference(user.getComprovanteEnderecoReference())
                .documentoIdentidadeReference(user.getDocumentoIdentidadeReference())
                .outrosArquivos(copyFileReferences(user.getOutrosArquivos()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private static Set<String> copyRoles(Set<String> roles) {
        return roles == null ? new HashSet<>() : new HashSet<>(roles);
    }

    private static List<String> copyStrings(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    private static List<User.UserFileReference> copyFileReferences(List<User.UserFileReference> refs) {
        if (refs == null) {
            return new ArrayList<>();
        }
        return refs.stream()
                .map(MongoUserRepository::normalizeReference)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static User.UserFileReference normalizeReference(User.UserFileReference reference) {
        if (reference == null) {
            return null;
        }
        if (reference.getStatus() == null) {
            reference.setStatus(User.DocumentReviewStatus.PENDING);
        }
        return reference;
    }

    @Override
    public Optional<User> findByIdAndOrganizationId(String id, String organizationId) {
        return repo.findByIdAndOrganizationId(id, organizationId).map(MongoUserRepository::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email).map(MongoUserRepository::toDomain);
    }

    @Override
    public List<User> findAllByOrganizationId(String organizationId) {
        return repo.findAllByOrganizationId(organizationId).stream()
                .map(MongoUserRepository::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        return toDomain(repo.save(toDoc(user)));
    }

    @Override
    public void deleteByIdAndOrganizationId(String id, String organizationId) {
        repo.deleteByIdAndOrganizationId(id, organizationId);
    }

    @Override
    public Optional<User> findAny() {
        return repo.findAll().stream().findFirst().map(MongoUserRepository::toDomain);
    }

    @Override
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return repo.existsById(id);
    }
}
