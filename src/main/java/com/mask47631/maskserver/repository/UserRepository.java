package com.mask47631.maskserver.repository;

import com.mask47631.maskserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findFirstByRole(String role);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
