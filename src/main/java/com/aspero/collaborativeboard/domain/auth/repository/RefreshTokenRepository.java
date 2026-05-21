package com.aspero.collaborativeboard.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aspero.collaborativeboard.domain.auth.entity.RefreshToken;
import com.aspero.collaborativeboard.domain.user.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
}