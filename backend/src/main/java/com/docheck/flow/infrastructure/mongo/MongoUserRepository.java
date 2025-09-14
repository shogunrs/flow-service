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
        return new User(d.id, d.name, d.email, d.passwordHash, d.roles, d.createdAt, d.updatedAt);
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
        return d;
    }

    @Override public Optional<User> findById(String id) { return repo.findById(id).map(MongoUserRepository::toDomain); }
    @Override public Optional<User> findByEmail(String email) { return repo.findByEmail(email).map(MongoUserRepository::toDomain); }
    @Override public List<User> findAll() { return repo.findAll().stream().map(MongoUserRepository::toDomain).collect(Collectors.toList()); }
    @Override public User save(User user) { return toDomain(repo.save(toDoc(user))); }
    @Override public void deleteById(String id) { repo.deleteById(id); }
}

