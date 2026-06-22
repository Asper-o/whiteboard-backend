package com.aspero.collaborativeboard.domain.canvas.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aspero.collaborativeboard.domain.canvas.dto.CanvasDTO;
import com.aspero.collaborativeboard.domain.canvas.entity.Canvas;
import com.aspero.collaborativeboard.domain.canvas.repository.CanvasRepository;
import com.aspero.collaborativeboard.domain.user.entity.User;
import com.aspero.collaborativeboard.domain.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CanvasService {

    private final CanvasRepository canvasRepository;
    private final UserRepository userRepository; // Assuming you have this!

    public CanvasService(CanvasRepository canvasRepository, UserRepository userRepository) {
        this.canvasRepository = canvasRepository;
        this.userRepository = userRepository;
    }

    // Helper method to grab the user who made the current HTTP request
    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        } else {
            throw new RuntimeException("Authentication Principal is not a User object!");
        }
    }

    // 1. GET ALL
    public List<CanvasDTO> getAllCanvasesForCurrentUser() {
        User currentUser = getAuthenticatedUser();
        List<Canvas> canvases = canvasRepository.findByUser(currentUser);
        
        return canvases.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 2. CREATE
    public CanvasDTO createCanvas(CanvasDTO dto) {
        User currentUser = getAuthenticatedUser();
        
        Canvas canvas = new Canvas();
        canvas.setName(dto.getName());
        canvas.setData(dto.getData());
        canvas.setUser(currentUser); 
        
        Canvas savedCanvas = canvasRepository.save(canvas);
        return mapToDTO(savedCanvas);
    }

    // 3. UPDATE
    public CanvasDTO updateCanvas(String id, CanvasDTO dto) {
        User currentUser = getAuthenticatedUser();
        
        Canvas canvas = canvasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canvas not found"));

        if (!canvas.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to edit this canvas");
        }

        canvas.setName(dto.getName());
        canvas.setData(dto.getData());
        
        Canvas updatedCanvas = canvasRepository.save(canvas);
        return mapToDTO(updatedCanvas);
    }

    // Helper mapper
    private CanvasDTO mapToDTO(Canvas canvas) {
        CanvasDTO dto = new CanvasDTO();
        dto.setId(canvas.getId());
        dto.setName(canvas.getName());
        dto.setData(canvas.getData());
        return dto;
    }
}