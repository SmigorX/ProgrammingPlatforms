package com.marketviz.repository;

import com.marketviz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** JPA repository for {@link User} entities. */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
