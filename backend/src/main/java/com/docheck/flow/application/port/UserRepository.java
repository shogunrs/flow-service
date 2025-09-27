package com.docheck.flow.application.port;

import com.docheck.flow.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByIdAndOrganizationId(String id, String organizationId);

    Optional<User> findByEmail(String email);

    List<User> findAllByOrganizationId(String organizationId);

    User save(User user);

    void deleteByIdAndOrganizationId(String id, String organizationId);

    // Novo método para obter um usuário qualquer (usado para fallback em desenvolvimento)
    Optional<User> findAny();

    boolean existsById(String id);
}
