package com.aspero.collaborativeboard.domain.canvas.entity;

import com.aspero.collaborativeboard.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Canvas {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    // Use LOB (Large Object) or TEXT so the database doesn't complain that the JSON string is too long!
    @Column(columnDefinition = "TEXT") 
    private String data;

    // Relate this to your User entity so each user only sees their own canvases!
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    
}