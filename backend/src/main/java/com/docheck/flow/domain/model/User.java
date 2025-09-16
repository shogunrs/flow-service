package com.docheck.flow.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class User {
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private Set<String> roles = new HashSet<>();

    // Documentos (opcionais)
    private String cpf;
    private String cnpj;
    private String rg;

    // Dados bancários (opcionais)
    private String banco;
    private String agencia;
    private String conta;
    private String tipoConta; // "CORRENTE", "POUPANCA"

    // PIX (opcionais)
    private String pixTipo; // "CPF", "CNPJ", "EMAIL", "TELEFONE", "CHAVE_ALEATORIA"
    private String pixChave;

    // Arquivos e biometria
    private String fotoPerfilUrl; // URL da foto de perfil
    private String comprovanteEnderecoUrl; // URL do comprovante de endereço
    private String faceEmbedding; // Embedding facial para reconhecimento
    private String ultimoIpAcesso; // Último IP de acesso
    private String ultimaLocalizacao; // Última localização (lat,lng)

    // Dados de auditoria
    private String ipCadastro; // IP usado no cadastro
    private String localizacaoCadastro; // Localização no cadastro (lat,lng)

    // Tags do usuário para categorização
    private List<String> tags;

    // Referências dos arquivos
    private UserFileReference fotoPerfilReference;
    private UserFileReference comprovanteEnderecoReference;
    private List<UserFileReference> outrosArquivos;

    private Instant createdAt;
    private Instant updatedAt;

    public User() {}

    public User(String id, String name, String email, String passwordHash, Set<String> roles, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        if (roles != null) this.roles = roles; else this.roles = new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    // Documentos
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    // Dados bancários
    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public String getConta() { return conta; }
    public void setConta(String conta) { this.conta = conta; }
    public String getTipoConta() { return tipoConta; }
    public void setTipoConta(String tipoConta) { this.tipoConta = tipoConta; }

    // PIX
    public String getPixTipo() { return pixTipo; }
    public void setPixTipo(String pixTipo) { this.pixTipo = pixTipo; }
    public String getPixChave() { return pixChave; }
    public void setPixChave(String pixChave) { this.pixChave = pixChave; }

    // Arquivos e biometria
    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }
    public String getComprovanteEnderecoUrl() { return comprovanteEnderecoUrl; }
    public void setComprovanteEnderecoUrl(String comprovanteEnderecoUrl) { this.comprovanteEnderecoUrl = comprovanteEnderecoUrl; }
    public String getFaceEmbedding() { return faceEmbedding; }
    public void setFaceEmbedding(String faceEmbedding) { this.faceEmbedding = faceEmbedding; }
    public String getUltimoIpAcesso() { return ultimoIpAcesso; }
    public void setUltimoIpAcesso(String ultimoIpAcesso) { this.ultimoIpAcesso = ultimoIpAcesso; }
    public String getUltimaLocalizacao() { return ultimaLocalizacao; }
    public void setUltimaLocalizacao(String ultimaLocalizacao) { this.ultimaLocalizacao = ultimaLocalizacao; }

    // Dados de auditoria
    public String getIpCadastro() { return ipCadastro; }
    public void setIpCadastro(String ipCadastro) { this.ipCadastro = ipCadastro; }
    public String getLocalizacaoCadastro() { return localizacaoCadastro; }
    public void setLocalizacaoCadastro(String localizacaoCadastro) { this.localizacaoCadastro = localizacaoCadastro; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Tags
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    // Referências dos arquivos
    public UserFileReference getFotoPerfilReference() { return fotoPerfilReference; }
    public void setFotoPerfilReference(UserFileReference fotoPerfilReference) { this.fotoPerfilReference = fotoPerfilReference; }
    public UserFileReference getComprovanteEnderecoReference() { return comprovanteEnderecoReference; }
    public void setComprovanteEnderecoReference(UserFileReference comprovanteEnderecoReference) { this.comprovanteEnderecoReference = comprovanteEnderecoReference; }
    public List<UserFileReference> getOutrosArquivos() { return outrosArquivos; }
    public void setOutrosArquivos(List<UserFileReference> outrosArquivos) { this.outrosArquivos = outrosArquivos; }

    // Classe interna para referências de arquivos
    public static class UserFileReference {
        private String objectKey;          // Chave do objeto no MinIO
        private String publicUrl;          // URL pública do arquivo
        private String filename;           // Nome original do arquivo
        private String fileType;           // Tipo do arquivo (PROFILE_PHOTO, ADDRESS_PROOF, DOCUMENT, IDENTITY, RG, CPF, FACE_RECOGNITION)
        private String contentType;        // MIME type do arquivo
        private long fileSize;             // Tamanho do arquivo em bytes
        private Instant uploadedAt;        // Data/hora do upload
        private String uploadedFromIp;     // IP de onde foi feito o upload
        private String uploadedFromLocation; // Localização do upload (lat,lng)
        private boolean isMobileUpload;    // Se foi enviado via celular

        public UserFileReference() {}

        public UserFileReference(String objectKey, String publicUrl, String filename, String fileType,
                               String contentType, long fileSize, Instant uploadedAt, String uploadedFromIp,
                               String uploadedFromLocation, boolean isMobileUpload) {
            this.objectKey = objectKey;
            this.publicUrl = publicUrl;
            this.filename = filename;
            this.fileType = fileType;
            this.contentType = contentType;
            this.fileSize = fileSize;
            this.uploadedAt = uploadedAt;
            this.uploadedFromIp = uploadedFromIp;
            this.uploadedFromLocation = uploadedFromLocation;
            this.isMobileUpload = isMobileUpload;
        }

        // Getters e Setters
        public String getObjectKey() { return objectKey; }
        public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
        public String getPublicUrl() { return publicUrl; }
        public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public Instant getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
        public String getUploadedFromIp() { return uploadedFromIp; }
        public void setUploadedFromIp(String uploadedFromIp) { this.uploadedFromIp = uploadedFromIp; }
        public String getUploadedFromLocation() { return uploadedFromLocation; }
        public void setUploadedFromLocation(String uploadedFromLocation) { this.uploadedFromLocation = uploadedFromLocation; }
        public boolean isMobileUpload() { return isMobileUpload; }
        public void setMobileUpload(boolean mobileUpload) { isMobileUpload = mobileUpload; }
    }

    // Métodos de conveniência para gerenciar arquivos
    public void addFileReference(UserFileReference fileReference) {
        if (outrosArquivos == null) {
            outrosArquivos = new ArrayList<>();
        }

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
            case "IDENTITY":
            case "RG":
            case "CPF":
            case "DOCUMENT":
            case "FACE_RECOGNITION":
                outrosArquivos.add(fileReference);
                break;
        }
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
        if (outrosArquivos != null) allFiles.addAll(outrosArquivos);
        return allFiles;
    }
}

