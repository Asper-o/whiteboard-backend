package com.aspero.collaborativeboard.domain.auth.controller;

import com.aspero.collaborativeboard.core.exception.RateLimitExceededException;
import com.aspero.collaborativeboard.core.security.ratelimit.RateLimitingService;
import com.aspero.collaborativeboard.domain.auth.dto.AuthResponse;
import com.aspero.collaborativeboard.domain.auth.dto.LoginRequest;
import com.aspero.collaborativeboard.domain.auth.dto.RegisterRequest;
import com.aspero.collaborativeboard.domain.auth.dto.TokenRefreshRequest;
import com.aspero.collaborativeboard.domain.auth.service.AuthService;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitingService rateLimitingService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
    
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        
        String ipAddress = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimitingService.resolveBucket(ipAddress);
        
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Too many login attempts. Please try again in 15 minutes.");
        }

        return authService.login(request);
    }
    
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return "Successfully logged out!";
    }
}