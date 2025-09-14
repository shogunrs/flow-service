package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    User save(User user);
    void deleteById(String id);
}

