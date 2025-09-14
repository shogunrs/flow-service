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

    public UserService(UserRepository repo) { this.repo = repo; }

    public Optional<User> findByEmail(String email) { return repo.findByEmail(email.toLowerCase()); }
    public List<User> list() { return repo.findAll(); }
    public Optional<User> get(String id) { return repo.findById(id); }

    public User create(String name, String email, String password, Set<String> roles) {
        Instant now = Instant.now();
        User u = new User(null, name, email.toLowerCase(), PasswordHasher.hash(password), roles, now, now);
        return repo.save(u);
    }

    public User update(String id, String name, Set<String> roles) {
        User existing = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        existing.setName(name);
        existing.setRoles(roles);
        existing.setUpdatedAt(Instant.now());
        return repo.save(existing);
    }

    public void delete(String id) { repo.deleteById(id); }

    public boolean verifyPassword(User user, String password) {
        return PasswordHasher.verify(password, user.getPasswordHash());
    }
}

