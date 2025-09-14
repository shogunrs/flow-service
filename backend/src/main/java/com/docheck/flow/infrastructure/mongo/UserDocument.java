package com.docheck.flow.infrastructure.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document("users")
public class UserDocument {
    @Id public String id;
    public String name;
    @Indexed(unique = true) public String email;
    public String passwordHash;
    public Set<String> roles;
    public Instant createdAt;
    public Instant updatedAt;
}

