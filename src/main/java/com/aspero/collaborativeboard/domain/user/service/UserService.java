package com.aspero.collaborativeboard.domain.user.service;

import com.aspero.collaborativeboard.domain.user.dto.PasswordUpdateRequest;
import com.aspero.collaborativeboard.domain.user.dto.UserDTO;
import com.aspero.collaborativeboard.domain.user.entity.User;
import com.aspero.collaborativeboard.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Helper method to securely get the currently logged-in user from the database
    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        } else {
            throw new RuntimeException("Authentication Principal is not a User object!");
        }
    }

    // 1. Get my own profile
    public UserDTO getMyProfile() {
        User currentUser = getAuthenticatedUser();
        return mapToDTO(currentUser);
    }

    // 2. Update my password
    public void updatePassword(PasswordUpdateRequest request) {
        User currentUser = getAuthenticatedUser();
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        
        currentUser.setPassword(hashedPassword);
        userRepository.save(currentUser);
    }

    // 3. Delete my account
    public void deleteMyAccount() {
        User currentUser = getAuthenticatedUser();
        userRepository.delete(currentUser);
    }

    // Helper mapper to convert a Database Entity into a safe DTO
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        // dto.setName(user.getName()); // Uncomment if you have a name field
        return dto;
    }
}