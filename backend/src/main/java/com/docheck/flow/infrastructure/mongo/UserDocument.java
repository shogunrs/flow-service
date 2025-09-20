package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("users")
public class UserDocument {
    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    private boolean superUser;

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
    private String fotoPerfilUrl;
    private String comprovanteEnderecoUrl;
    private String documentoIdentidade;
    private String faceEmbedding;
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

    // Documentos da empresa
    private String cartaoCnpjImage;
    private String contratoSocialImage;
    private String qualificacaoSociosImage;

    // Dados de auditoria
    private String ipCadastro;
    private String localizacaoCadastro;

    // Tags e arquivos
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private User.UserFileReference fotoPerfilReference;
    private User.UserFileReference comprovanteEnderecoReference;
    private User.UserFileReference documentoIdentidadeReference;
    @Builder.Default
    private List<User.UserFileReference> outrosArquivos = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}
