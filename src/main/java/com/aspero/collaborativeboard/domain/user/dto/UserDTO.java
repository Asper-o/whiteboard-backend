package com.aspero.collaborativeboard.domain.user.dto;

import lombok.Data;

@Data
public class UserDTO {
    // Change this to Long if your User ID is a number!
    private Long id; 
    private String email;
    
    // If your User entity has a name or username, add it here:
    // private String name; 
}
