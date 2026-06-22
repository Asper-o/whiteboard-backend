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
        
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());
        
        refreshToken.setUser(user);
        refreshToken.setToken(java.util.UUID.randomUUID().toString()); 
        refreshToken.setExpiryDate(Instant.now().plusMillis(1000 * 60 * 60 * 24 * 7)); // 7 days
        
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(jwt, refreshToken.getToken());
    }
    
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        if (existingToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(existingToken);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }

        User user = existingToken.getUser();
      
        existingToken.setToken(java.util.UUID.randomUUID().toString());
        existingToken.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7)); // Resets the 7-day clock
        
        refreshTokenRepository.save(existingToken);
        String newAccessToken = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(newAccessToken, existingToken.getToken());
    }
    
    @Transactional
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(jwt);
            blacklistedTokenRepository.save(blacklistedToken);

            String email = jwtUtil.extractEmail(jwt);
            userRepository.findByEmail(email).ifPresent(refreshTokenRepository::deleteByUser);
        }
        
    }
}