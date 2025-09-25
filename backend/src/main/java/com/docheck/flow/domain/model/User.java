package com.docheck.flow.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String id;
    private String name;
    private String email;
    private String passwordHash;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private boolean superUser = false;

    // Hierarquia e Multi-Tenancy
    private String organizationId;
    private String createdBy; // ID do usuário que criou este usuário

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
    private UserFileReference cartaoCnpjReference;
    private UserFileReference contratoSocialReference;
    private UserFileReference qualificacaoSociosReference;

    // Dados de auditoria
    private String ipCadastro;
    private String localizacaoCadastro;

    // Tags do usuário para categorização
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // Referências dos arquivos
    private UserFileReference fotoPerfilReference;
    private UserFileReference comprovanteEnderecoReference;
    private UserFileReference documentoIdentidadeReference;

    @Builder.Default
    private List<UserFileReference> outrosArquivos = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    // Construtor específico para compatibilidade
    public User(String id, String name, String email, String passwordHash, Set<String> roles, boolean superUser, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles != null ? roles : new HashSet<>();
        this.superUser = superUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = new ArrayList<>();
        this.outrosArquivos = new ArrayList<>();
    }

    // Métodos de conveniência para gerenciar arquivos
    public void addFileReference(UserFileReference fileReference) {
        if (outrosArquivos == null) {
            outrosArquivos = new ArrayList<>();
        }

        resetReviewMetadata(fileReference);

        // Definir onde salvar baseado no tipo
        switch (fileReference.getFileType()) {
            case "PROFILE_PHOTO":
                this.fotoPerfilReference = fileReference;
                this.fotoPerfilUrl = fileReference.getPublicUrl();
                break;
            case "ADDRESS_PROOF":
                this.comprovanteEnderecoReference = fileReference;
                this.comprovanteEnderecoUrl = fileReference.getPublicUrl();
                break;
            case "DOCUMENT":
            case "IDENTITY":
                this.documentoIdentidadeReference = fileReference;
                this.documentoIdentidade = fileReference.getPublicUrl();
                break;
            case "CONTRATO_SOCIAL":
                this.contratoSocialReference = fileReference;
                this.contratoSocialImage = fileReference.getPublicUrl();
                break;
            case "CARTAO_CNPJ":
                this.cartaoCnpjReference = fileReference;
                this.cartaoCnpjImage = fileReference.getPublicUrl();
                break;
            case "QUALIFICACAO_SOCIOS":
                this.qualificacaoSociosReference = fileReference;
                this.qualificacaoSociosImage = fileReference.getPublicUrl();
                break;
            case "RG":
            case "CPF":
            case "FACE_RECOGNITION":
            default:
                outrosArquivos.add(fileReference);
                break;
        }
    }

    private void resetReviewMetadata(UserFileReference ref) {
        if (ref == null) return;
        ref.setStatus(DocumentReviewStatus.PENDING);
        ref.setReviewerId(null);
        ref.setReviewerName(null);
        ref.setReviewedAt(null);
        ref.setReviewNotes(null);
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    public UserFileReference getFileByType(String fileType) {
        switch (fileType) {
            case "PROFILE_PHOTO":
                return fotoPerfilReference;
            case "ADDRESS_PROOF":
                return comprovanteEnderecoReference;
            case "DOCUMENT":
            case "IDENTITY":
                return documentoIdentidadeReference;
            case "CARTAO_CNPJ":
                return cartaoCnpjReference;
            case "CONTRATO_SOCIAL":
                return contratoSocialReference;
            case "QUALIFICACAO_SOCIOS":
                return qualificacaoSociosReference;
            default:
                if (outrosArquivos != null) {
                    return outrosArquivos.stream()
                        .filter(ref -> fileType.equals(ref.getFileType()))
                        .findFirst()
                        .orElse(null);
                }
                return null;
        }
    }

    public List<UserFileReference> getAllFiles() {
        List<UserFileReference> allFiles = new ArrayList<>();
        if (fotoPerfilReference != null) allFiles.add(fotoPerfilReference);
        if (comprovanteEnderecoReference != null) allFiles.add(comprovanteEnderecoReference);
        if (documentoIdentidadeReference != null) allFiles.add(documentoIdentidadeReference);
        if (cartaoCnpjReference != null) allFiles.add(cartaoCnpjReference);
        if (contratoSocialReference != null) allFiles.add(contratoSocialReference);
        if (qualificacaoSociosReference != null) allFiles.add(qualificacaoSociosReference);
        if (outrosArquivos != null) allFiles.addAll(outrosArquivos);
        return allFiles;
    }

    // Classe interna para referências de arquivos
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserFileReference {
        private String objectKey;          // Chave do objeto no MinIO
        private String publicUrl;          // URL pública do arquivo
        private String filename;           // Nome original do arquivo
        private String fileType;           // Tipo do arquivo (PROFILE_PHOTO, ADDRESS_PROOF, DOCUMENT, IDENTITY, RG, CPF, FACE_RECOGNITION, CONTRATO_SOCIAL, CARTAO_CNPJ, QUALIFICACAO_SOCIOS)
        private String contentType;        // MIME type do arquivo
        private long fileSize;             // Tamanho do arquivo em bytes
        private Instant uploadedAt;        // Data/hora do upload
        private String uploadedFromIp;     // IP de onde foi feito o upload
        private String uploadedFromLocation; // Localização do upload (lat,lng)
        private boolean isMobileUpload;    // Se foi enviado via celular
        @Builder.Default
        private DocumentReviewStatus status = DocumentReviewStatus.PENDING;
        private String reviewerId;
        private String reviewerName;
        private Instant reviewedAt;
        private String reviewNotes;
    }

    public enum DocumentReviewStatus {
        PENDING, APPROVED, REJECTED
    }
}
