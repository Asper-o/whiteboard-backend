package com.aspero.collaborativeboard.domain.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "blacklisted_tokens")
@Data
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token; // The JWT string
}