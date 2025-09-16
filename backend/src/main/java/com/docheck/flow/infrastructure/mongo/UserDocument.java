package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.docheck.flow.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Document("users")
public class UserDocument {
    @Id public String id;
    public String name;
    @Indexed(unique = true) public String email;
    public String passwordHash;
    public Set<String> roles;

    // Documentos (opcionais)
    public String cpf;
    public String cnpj;
    public String rg;

    // Dados bancários (opcionais)
    public String banco;
    public String agencia;
    public String conta;
    public String tipoConta;

    // PIX (opcionais)
    public String pixTipo;
    public String pixChave;

    // Arquivos e biometria
    public String fotoPerfilUrl;
    public String comprovanteEnderecoUrl;
    public String faceEmbedding;
    public String ultimoIpAcesso;
    public String ultimaLocalizacao;

    // Dados de auditoria
    public String ipCadastro;
    public String localizacaoCadastro;

    // Tags do usuário para categorização
    public List<String> tags;

    // Referências dos arquivos detalhadas
    public User.UserFileReference fotoPerfilReference;
    public User.UserFileReference comprovanteEnderecoReference;
    public List<User.UserFileReference> outrosArquivos;

    public Instant createdAt;
    public Instant updatedAt;
}

