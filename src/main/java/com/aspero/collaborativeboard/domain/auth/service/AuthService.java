package com.aspero.collaborativeboard.domain.auth.service;

import com.aspero.collaborativeboard.domain.user.entity.User;
import com.aspero.collaborativeboard.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;

import com.aspero.collaborativeboard.core.exception.InvalidCredentialsException;
import com.aspero.collaborativeboard.core.exception.TokenRefreshException;
import com.aspero.collaborativeboard.core.exception.UserAlreadyExistsException;
import com.aspero.collaborativeboard.core.security.JwtUtil;
import com.aspero.collaborativeboard.domain.auth.dto.AuthResponse;
import com.aspero.collaborativeboard.domain.auth.dto.LoginRequest;
import com.aspero.collaborativeboard.domain.auth.dto.RegisterRequest;
import com.aspero.collaborativeboard.domain.auth.dto.TokenRefreshRequest;
import com.aspero.collaborativeboard.domain.auth.entity.BlacklistedToken;
import com.aspero.collaborativeboard.domain.auth.entity.RefreshToken;
import com.aspero.collaborativeboard.domain.auth.repository.BlacklistedTokenRepository;
import com.aspero.collaborativeboard.domain.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        		throw new UserAlreadyExistsException("A user with this email already exists!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        // Hashing the password before saving!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        userRepository.save(user);
        return "User registered successfully!";
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String jwt = jwtUtil.generateToken(user.getEmail());
        
        //Find existing token OR create a new one if it doesn't exist
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());
        
        refreshToken.setUser(user);
        refreshToken.setToken(java.util.UUID.randomUUID().toString()); // Secure random string
        refreshToken.setExpiryDate(Instant.now().plusMillis(1000 * 60 * 60 * 24 * 7)); // 7 days
        
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(jwt, refreshToken.getToken());
    }
    
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        // 1. Find the old refresh token
        RefreshToken existingToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        // 2. Check if it's expired
        if (existingToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(existingToken);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }

        User user = existingToken.getUser();

        // --- THE ROTATION LOGIC (UPDATED) ---
        
        // 3. Instead of deleting and recreating, we simply UPDATE the existing database row.
        // This prevents the SQL "Duplicate entry" constraint crash!
        existingToken.setToken(java.util.UUID.randomUUID().toString());
        existingToken.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7)); // Resets the 7-day clock
        
        refreshTokenRepository.save(existingToken);

        // 4. Generate the new 15-minute Access Token
        String newAccessToken = jwtUtil.generateToken(user.getEmail());

        // 5. Send BOTH new tokens back to Angular
        return new AuthResponse(newAccessToken, existingToken.getToken());
    }
    
    @Transactional
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            
            // 1. Blacklist the JWT so it can't be used again
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(jwt);
            blacklistedTokenRepository.save(blacklistedToken);

            // 2. Delete the user's Refresh Token from the DB
            String email = jwtUtil.extractEmail(jwt);
            userRepository.findByEmail(email).ifPresent(refreshTokenRepository::deleteByUser);
        }
        
    }
}