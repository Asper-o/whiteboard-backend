package com.aspero.collaborativeboard.core.security;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // <-- Add this to inject the filter
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter; // <-- Inject our new Bouncer

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        		
            .csrf(csrf -> csrf.disable())
            // Tell Spring we are stateless (No RAM sessions, only JWTs)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() 
                .anyRequest().authenticated() 
            )
            // Put our Bouncer BEFORE the standard Spring Security password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    // Add this inside your SecurityConfig class
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Explicitly allow your Angular app
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        
        // Allow the browser to send these specific HTTP methods (Crucial for the OPTIONS preflight)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers (like Authorization for your JWT)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (important if we upgrade to HttpOnly cookies later)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this rule to every single endpoint in your app
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}