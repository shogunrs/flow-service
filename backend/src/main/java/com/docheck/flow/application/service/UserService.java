package com.docheck.flow.application.service;

import com.docheck.flow.application.port.UserRepository;
import com.docheck.flow.domain.model.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
        return repo.findByEmail(email.toLowerCase());
    }

    public List<User> list() {
        return repo.findAll();
    }

    public Optional<User> get(String id) {
        return repo.findById(id);
    }

    public User create(String name, String email, String password, Set<String> roles,
            String cpf, String cnpj, String rg,
            String banco, String agencia, String conta, String tipoConta,
            String pixTipo, String pixChave,
            String fotoPerfilUrl, String comprovanteEnderecoUrl,
            String ipCadastro, String localizacaoCadastro) {
        Instant now = Instant.now();
        User u = new User(null, name, email.toLowerCase(), PasswordHasher.hash(password), roles, now, now);

        // Documentos
        u.setCpf(cpf);
        u.setCnpj(cnpj);
        u.setRg(rg);

        // Dados bancários
        u.setBanco(banco);
        u.setAgencia(agencia);
        u.setConta(conta);
        u.setTipoConta(tipoConta);

        // PIX
        u.setPixTipo(pixTipo);
        u.setPixChave(pixChave);

        // Arquivos
        u.setFotoPerfilUrl(fotoPerfilUrl);
        u.setComprovanteEnderecoUrl(comprovanteEnderecoUrl);

        // Dados de auditoria
        u.setIpCadastro(ipCadastro);
        u.setLocalizacaoCadastro(localizacaoCadastro);

        return repo.save(u);
    }

    public User update(String id, String name, String email, Set<String> roles,
            String cpf, String cnpj, String rg,
            String banco, String agencia, String conta, String tipoConta,
            String pixTipo, String pixChave) {
        User existing = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));

        existing.setName(name);

        // Apenas atualizar o email se ele for diferente do atual
        if (email != null && !email.equalsIgnoreCase(existing.getEmail())) {
            // Verificar se já existe outro usuário com esse email
            Optional<User> existingWithEmail = repo.findByEmail(email.toLowerCase());
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("Email já está em uso por outro usuário");
            }
            existing.setEmail(email.toLowerCase());
        }
        existing.setRoles(roles);

        // Documentos
        existing.setCpf(cpf);
        existing.setCnpj(cnpj);
        existing.setRg(rg);

        // Dados bancários
        existing.setBanco(banco);
        existing.setAgencia(agencia);
        existing.setConta(conta);
        existing.setTipoConta(tipoConta);

        // PIX
        existing.setPixTipo(pixTipo);
        existing.setPixChave(pixChave);

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
}
