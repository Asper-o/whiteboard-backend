package com.aspero.collaborativeboard.domain.user.controller;

import com.aspero.collaborativeboard.domain.user.dto.PasswordUpdateRequest;
import com.aspero.collaborativeboard.domain.user.dto.UserDTO;
import com.aspero.collaborativeboard.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. Get my own profile
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    // 2. Update my password
    @PutMapping("/me/password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok("Password successfully updated!");
    }

    // 3. Delete my account
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount() {
        userService.deleteMyAccount();
        return ResponseEntity.ok("Account successfully deleted!");
    }
}