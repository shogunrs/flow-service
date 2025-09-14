package com.docheck.flow.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class User {
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private Set<String> roles = new HashSet<>();
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

