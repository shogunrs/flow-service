package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.application.port.UserRepository;
import com.docheck.flow.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoUserRepository implements UserRepository {
    private final SpringDataUserRepository repo;

    public MongoUserRepository(SpringDataUserRepository repo) { this.repo = repo; }

    private static User toDomain(UserDocument d) {
        if (d == null) return null;
        User u = new User(d.id, d.name, d.email, d.passwordHash, d.roles, d.createdAt, d.updatedAt);

        // Documentos
        u.setCpf(d.cpf);
        u.setCnpj(d.cnpj);
        u.setRg(d.rg);

        // Dados bancários
        u.setBanco(d.banco);
        u.setAgencia(d.agencia);
        u.setConta(d.conta);
        u.setTipoConta(d.tipoConta);

        // PIX
        u.setPixTipo(d.pixTipo);
        u.setPixChave(d.pixChave);

        // Arquivos e biometria
        u.setFotoPerfilUrl(d.fotoPerfilUrl);
        u.setComprovanteEnderecoUrl(d.comprovanteEnderecoUrl);
        u.setFaceEmbedding(d.faceEmbedding);
        u.setUltimoIpAcesso(d.ultimoIpAcesso);
        u.setUltimaLocalizacao(d.ultimaLocalizacao);

        // Dados de auditoria
        u.setIpCadastro(d.ipCadastro);
        u.setLocalizacaoCadastro(d.localizacaoCadastro);

        return u;
    }

    private static UserDocument toDoc(User u) {
        UserDocument d = new UserDocument();
        d.id = u.getId();
        d.name = u.getName();
        d.email = u.getEmail();
        d.passwordHash = u.getPasswordHash();
        d.roles = u.getRoles();
        d.createdAt = u.getCreatedAt();
        d.updatedAt = u.getUpdatedAt();

        // Documentos
        d.cpf = u.getCpf();
        d.cnpj = u.getCnpj();
        d.rg = u.getRg();

        // Dados bancários
        d.banco = u.getBanco();
        d.agencia = u.getAgencia();
        d.conta = u.getConta();
        d.tipoConta = u.getTipoConta();

        // PIX
        d.pixTipo = u.getPixTipo();
        d.pixChave = u.getPixChave();

        // Arquivos e biometria
        d.fotoPerfilUrl = u.getFotoPerfilUrl();
        d.comprovanteEnderecoUrl = u.getComprovanteEnderecoUrl();
        d.faceEmbedding = u.getFaceEmbedding();
        d.ultimoIpAcesso = u.getUltimoIpAcesso();
        d.ultimaLocalizacao = u.getUltimaLocalizacao();

        // Dados de auditoria
        d.ipCadastro = u.getIpCadastro();
        d.localizacaoCadastro = u.getLocalizacaoCadastro();

        return d;
    }

    @Override public Optional<User> findById(String id) { return repo.findById(id).map(MongoUserRepository::toDomain); }
    @Override public Optional<User> findByEmail(String email) { return repo.findByEmail(email).map(MongoUserRepository::toDomain); }
    @Override public List<User> findAll() { return repo.findAll().stream().map(MongoUserRepository::toDomain).collect(Collectors.toList()); }
    @Override public User save(User user) { return toDomain(repo.save(toDoc(user))); }
    @Override public void deleteById(String id) { repo.deleteById(id); }
}

