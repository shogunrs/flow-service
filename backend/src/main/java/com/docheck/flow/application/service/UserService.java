package com.docheck.flow.application.service;

import com.docheck.flow.application.port.UserRepository;
import com.docheck.flow.application.service.command.UserWriteCommand;
import com.docheck.flow.domain.model.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return repo.findByEmail(email.toLowerCase());
    }

    public List<User> list() {
        return repo.findAll();
    }

    public Optional<User> get(String id) {
        return repo.findById(id);
    }

    public User create(UserWriteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        String email = normalizeEmail(command.getEmail());
        String password = normalizePassword(command.getRawPassword());
        Instant now = Instant.now();

        Set<String> roles = copyRoles(command.getRoles());
        boolean superUser = Boolean.TRUE.equals(command.getSuperUser());
        String fotoPerfil = resolveProfilePhoto(command);

        User user = User.builder()
                .name(command.getName())
                .email(email)
                .passwordHash(PasswordHasher.hash(password))
                .roles(roles)
                .superUser(superUser)
                .cpf(command.getCpf())
                .cnpj(command.getCnpj())
                .rg(command.getRg())
                .telefone(command.getTelefone())
                .endereco(command.getEndereco())
                .enderecoRua(command.getEnderecoRua())
                .enderecoNumero(command.getEnderecoNumero())
                .enderecoComplemento(command.getEnderecoComplemento())
                .enderecoBairro(command.getEnderecoBairro())
                .enderecoCidade(command.getEnderecoCidade())
                .enderecoEstado(command.getEnderecoEstado())
                .cep(command.getCep())
                .banco(command.getBanco())
                .agencia(command.getAgencia())
                .conta(command.getConta())
                .tipoConta(command.getTipoConta())
                .pixTipo(command.getPixTipo())
                .pixChave(command.getPixChave())
                .fotoPerfilUrl(fotoPerfil)
                .comprovanteEnderecoUrl(command.getComprovanteEnderecoUrl())
                .documentoIdentidade(command.getDocumentoIdentidade())
                .observacoes(command.getObservacoes())
                .nomeCompleto(command.getNomeCompleto())
                .quantidadeSocios(command.getQuantidadeSocios())
                .enderecoEmpresa(command.getEnderecoEmpresa())
                .enderecoEmpresaRua(command.getEnderecoEmpresaRua())
                .enderecoEmpresaNumero(command.getEnderecoEmpresaNumero())
                .enderecoEmpresaComplemento(command.getEnderecoEmpresaComplemento())
                .enderecoEmpresaBairro(command.getEnderecoEmpresaBairro())
                .enderecoEmpresaCidade(command.getEnderecoEmpresaCidade())
                .enderecoEmpresaEstado(command.getEnderecoEmpresaEstado())
                .cepEmpresa(command.getCepEmpresa())
                .observacoesEmpresa(command.getObservacoesEmpresa())
                .razaoSocial(command.getRazaoSocial())
                .nomeFantasia(command.getNomeFantasia())
                .cartaoCnpjImage(command.getCartaoCnpjImage())
                .contratoSocialImage(command.getContratoSocialImage())
                .qualificacaoSociosImage(command.getQualificacaoSociosImage())
                .ipCadastro(command.getIpCadastro())
                .localizacaoCadastro(command.getLocalizacaoCadastro())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return repo.save(user);
    }

    public User update(UserWriteCommand command) {
        if (command == null || isBlank(command.getId())) {
            throw new IllegalArgumentException("command id must not be null");
        }

        User existing = repo.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!isBlank(command.getName())) {
            existing.setName(command.getName());
        }

        if (!isBlank(command.getEmail()) && !command.getEmail().equalsIgnoreCase(existing.getEmail())) {
            String normalizedEmail = normalizeEmail(command.getEmail());
            Optional<User> existingWithEmail = repo.findByEmail(normalizedEmail);
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(existing.getId())) {
                throw new IllegalArgumentException("Email já está em uso por outro usuário");
            }
            existing.setEmail(normalizedEmail);
        }

        if (command.getRoles() != null) {
            existing.setRoles(copyRoles(command.getRoles()));
        }
        existing.setSuperUser(Boolean.TRUE.equals(command.getSuperUser()));

        existing.setCpf(command.getCpf());
        existing.setCnpj(command.getCnpj());
        existing.setRg(command.getRg());
        existing.setTelefone(command.getTelefone());

        existing.setEndereco(command.getEndereco());
        existing.setEnderecoRua(command.getEnderecoRua());
        existing.setEnderecoNumero(command.getEnderecoNumero());
        existing.setEnderecoComplemento(command.getEnderecoComplemento());
        existing.setEnderecoBairro(command.getEnderecoBairro());
        existing.setEnderecoCidade(command.getEnderecoCidade());
        existing.setEnderecoEstado(command.getEnderecoEstado());
        existing.setCep(command.getCep());

        existing.setBanco(command.getBanco());
        existing.setAgencia(command.getAgencia());
        existing.setConta(command.getConta());
        existing.setTipoConta(command.getTipoConta());

        existing.setPixTipo(command.getPixTipo());
        existing.setPixChave(command.getPixChave());

        String fotoPerfil = resolveProfilePhoto(command);
        if (fotoPerfil != null) {
            existing.setFotoPerfilUrl(fotoPerfil);
        }
        if (command.getComprovanteEnderecoUrl() != null) {
            existing.setComprovanteEnderecoUrl(command.getComprovanteEnderecoUrl());
        }
        if (command.getDocumentoIdentidade() != null) {
            existing.setDocumentoIdentidade(command.getDocumentoIdentidade());
        }

        existing.setObservacoes(command.getObservacoes());
        existing.setNomeCompleto(command.getNomeCompleto());

        existing.setQuantidadeSocios(command.getQuantidadeSocios());
        existing.setEnderecoEmpresa(command.getEnderecoEmpresa());
        existing.setEnderecoEmpresaRua(command.getEnderecoEmpresaRua());
        existing.setEnderecoEmpresaNumero(command.getEnderecoEmpresaNumero());
        existing.setEnderecoEmpresaComplemento(command.getEnderecoEmpresaComplemento());
        existing.setEnderecoEmpresaBairro(command.getEnderecoEmpresaBairro());
        existing.setEnderecoEmpresaCidade(command.getEnderecoEmpresaCidade());
        existing.setEnderecoEmpresaEstado(command.getEnderecoEmpresaEstado());
        existing.setCepEmpresa(command.getCepEmpresa());
        existing.setObservacoesEmpresa(command.getObservacoesEmpresa());
        existing.setRazaoSocial(command.getRazaoSocial());
        existing.setNomeFantasia(command.getNomeFantasia());

        if (command.getCartaoCnpjImage() != null) {
            existing.setCartaoCnpjImage(command.getCartaoCnpjImage());
        }
        if (command.getContratoSocialImage() != null) {
            existing.setContratoSocialImage(command.getContratoSocialImage());
        }
        if (command.getQualificacaoSociosImage() != null) {
            existing.setQualificacaoSociosImage(command.getQualificacaoSociosImage());
        }

        existing.setUpdatedAt(Instant.now());
        return repo.save(existing);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

    public boolean verifyPassword(User user, String password) {
        return PasswordHasher.verify(password, user.getPasswordHash());
    }

    public void updateAccessInfo(String userId, String ipAddress, String location) {
        User user = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setUltimoIpAcesso(ipAddress);
        user.setUltimaLocalizacao(location);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public void updateFaceEmbedding(String userId, String faceEmbedding) {
        User user = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFaceEmbedding(faceEmbedding);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public void addFileReference(String userId, String objectKey, String publicUrl, String filename,
            String fileType, String contentType, long fileSize,
            String uploadedFromIp, String uploadedFromLocation, boolean isMobileUpload) {
        User user = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        User.UserFileReference fileReference = new User.UserFileReference(
                objectKey, publicUrl, filename, fileType, contentType, fileSize,
                Instant.now(), uploadedFromIp, uploadedFromLocation, isMobileUpload);

        user.addFileReference(fileReference);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public void addUserTag(String userId, String tag) {
        User user = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.addTag(tag);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public void removeUserTag(String userId, String tag) {
        User user = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.removeTag(tag);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public List<User.UserFileReference> getUserFiles(String userId) {
        User user = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getAllFiles();
    }

    public User.UserFileReference getUserFileByType(String userId, String fileType) {
        User user = repo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getFileByType(fileType);
    }

    private static String normalizeEmail(String email) {
        if (isBlank(email)) {
            throw new IllegalArgumentException("Email must not be empty");
        }
        return email.trim().toLowerCase();
    }

    private static String normalizePassword(String password) {
        if (isBlank(password)) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return password.trim();
    }

    private static Set<String> copyRoles(Set<String> roles) {
        return roles == null ? new HashSet<>() : new HashSet<>(roles);
    }

    private static String resolveProfilePhoto(UserWriteCommand command) {
        if (command.getProfileImage() != null) {
            return command.getProfileImage();
        }
        return command.getFotoPerfilUrl();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
