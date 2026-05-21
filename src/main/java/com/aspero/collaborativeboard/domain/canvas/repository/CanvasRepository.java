package com.aspero.collaborativeboard.domain.canvas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aspero.collaborativeboard.domain.canvas.entity.Canvas;
import com.aspero.collaborativeboard.domain.user.entity.User;

import java.util.List;

@Repository
public interface CanvasRepository extends JpaRepository<Canvas, String> {
    // This automatically generates the SQL to find all canvases for a specific user
    List<Canvas> findByUser(User user);
}