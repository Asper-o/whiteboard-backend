package com.aspero.collaborativeboard.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aspero.collaborativeboard.domain.auth.entity.BlacklistedToken;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    boolean existsByToken(String token);
}
				