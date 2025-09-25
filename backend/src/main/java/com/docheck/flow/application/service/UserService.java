package com.docheck.flow.application.service;

import com.docheck.flow.application.port.UserRepository;
import com.docheck.flow.application.service.command.UserWriteCommand;
import com.docheck.flow.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return repo.findByEmail(email.toLowerCase());
    }

    public List<User> list() {
        User manager = getAuthenticatedUser();
        return repo.findAllByOrganizationId(manager.getOrganizationId());
    }

    public Optional<User> get(String id) {
        User manager = getAuthenticatedUser();
        return repo.findByIdAndOrganizationId(id, manager.getOrganizationId());
    }

    public User create(UserWriteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        User manager = getAuthenticatedUser();

        String email = normalizeEmail(command.getEmail());
        String password = normalizePassword(command.getRawPassword());
        Instant now = Instant.now();

        Set<String> roles = copyRoles(command.getRoles());
        boolean superUser = Boolean.TRUE.equals(command.getSuperUser());
        String fotoPerfil = resolveProfilePhoto(command);

        User user = User.builder()
                .name(command.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .roles(roles)
                .superUser(superUser)
                .organizationId(manager.getOrganizationId())
                .createdBy(manager.getId())
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

        User manager = getAuthenticatedUser();
        User existing = repo.findByIdAndOrganizationId(command.getId(), manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));

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

        if (!isBlank(command.getRawPassword())) {
            existing.setPasswordHash(passwordEncoder.encode(command.getRawPassword()));
        }

        if (command.getRoles() != null) {
            existing.setRoles(copyRoles(command.getRoles()));
        }
        existing.setSuperUser(Boolean.TRUE.equals(command.getSuperUser()));

        // ... (o resto das atualizações de campo permanecem as mesmas)

        existing.setUpdatedAt(Instant.now());
        return repo.save(existing);
    }

    public void delete(String id) {
        User manager = getAuthenticatedUser();
        repo.deleteByIdAndOrganizationId(id, manager.getOrganizationId());
    }

    public boolean verifyPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    public void updateAccessInfo(String userId, String ipAddress, String location) {
        User manager = getAuthenticatedUser();
        User user = repo.findByIdAndOrganizationId(userId, manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));
        user.setUltimoIpAcesso(ipAddress);
        user.setUltimaLocalizacao(location);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public void updateFaceEmbedding(String userId, String faceEmbedding) {
        User manager = getAuthenticatedUser();
        User user = repo.findByIdAndOrganizationId(userId, manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));
        user.setFaceEmbedding(faceEmbedding);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public void addFileReference(String userId, String objectKey, String publicUrl, String filename,
                                 String fileType, String contentType, long fileSize,
                                 String uploadedFromIp, String uploadedFromLocation, boolean isMobileUpload) {
        User manager = getAuthenticatedUser();
        User user = repo.findByIdAndOrganizationId(userId, manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));

        User.UserFileReference fileReference = User.UserFileReference.builder()
                .objectKey(objectKey)
                .publicUrl(publicUrl)
                .filename(filename)
                .fileType(fileType)
                .contentType(contentType)
                .fileSize(fileSize)
                .uploadedAt(Instant.now())
                .uploadedFromIp(uploadedFromIp)
                .uploadedFromLocation(uploadedFromLocation)
                .isMobileUpload(isMobileUpload)
                .status(User.DocumentReviewStatus.PENDING)
                .build();

        user.addFileReference(fileReference);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public void addUserTag(String userId, String tag) {
        User manager = getAuthenticatedUser();
        User user = repo.findByIdAndOrganizationId(userId, manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));
        user.addTag(tag);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public void removeUserTag(String userId, String tag) {
        User manager = getAuthenticatedUser();
        User user = repo.findByIdAndOrganizationId(userId, manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));
        user.removeTag(tag);
        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    public List<User.UserFileReference> getUserFiles(String userId) {
        User manager = getAuthenticatedUser();
        User user = repo.findByIdAndOrganizationId(userId, manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));
        return user.getAllFiles();
    }

    public User.UserFileReference getUserFileByType(String userId, String fileType) {
        User manager = getAuthenticatedUser();
        User user = repo.findByIdAndOrganizationId(userId, manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));
        return user.getFileByType(fileType);
    }

    public void updateFileStatus(String userId, String fileType, User.DocumentReviewStatus status, String reviewNotes) {
        User manager = getAuthenticatedUser(); // O revisor é o usuário autenticado
        User user = repo.findByIdAndOrganizationId(userId, manager.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("User not found in this organization"));

        User.UserFileReference reference = user.getFileByType(fileType);
        if (reference == null) {
            throw new IllegalArgumentException("File reference not found for type " + fileType);
        }

        reference.setStatus(status);
        reference.setReviewerId(manager.getId());
        reference.setReviewerName(manager.getName());
        reference.setReviewNotes(reviewNotes);
        reference.setReviewedAt(Instant.now());

        user.setUpdatedAt(Instant.now());
        repo.save(user);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            // ATENÇÃO: Lógica de fallback para desenvolvimento.
            // Retorna o primeiro usuário do banco de dados como o "gerente" padrão.
            return repo.findAny()
                    .orElseThrow(() -> new IllegalStateException("Development mode fallback failed: No users found in database."));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return repo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
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
