package com.aspero.collaborativeboard.core.security;

import com.aspero.collaborativeboard.domain.auth.repository.BlacklistedTokenRepository;
import com.aspero.collaborativeboard.domain.user.entity.User;
import com.aspero.collaborativeboard.domain.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 1. Look for the "Authorization" header in the incoming request
        final String authHeader = request.getHeader("Authorization");

        // 2. If there's no header, or it doesn't start with "Bearer ", let it pass 
        // (Spring Security will block it later if the route is protected)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token (Remove "Bearer " from the string)
        final String jwt = authHeader.substring(7);
        
        // 4. If the token is in the blacklist, block them immediately!
        if (blacklistedTokenRepository.existsByToken(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been logged out.");
            return; // Stop the request
        }

        // 5. Try to validate the token, but catch the expiration safely
        try {
            if (jwtUtil.isTokenValid(jwt)) {
                String email = jwtUtil.extractEmail(jwt);
                
                // Tell Spring Security: "This user is verified and logged in for this request!"
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findByEmail(email).orElse(null);
                    
                    if (user != null) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user, null, Collections.emptyList()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            // CAUGHT IT! Return a clean 401 instead of crashing with a 500
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT token has expired");
            return;
        } catch (Exception e) {
            // Catch any other weird token issues (like malformed text)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT token");
            return;
        }

        // 7. Continue to the next step
        filterChain.doFilter(request, response);
    }
}