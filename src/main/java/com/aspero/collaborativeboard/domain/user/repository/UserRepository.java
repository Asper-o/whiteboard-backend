package com.aspero.collaborativeboard.domain.user.repository;

import com.aspero.collaborativeboard.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // This magic method allows us to find a user by email with zero SQL written!
    Optional<User> findByEmail(String email);
}