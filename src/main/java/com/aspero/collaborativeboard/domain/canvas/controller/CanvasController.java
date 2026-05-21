package com.aspero.collaborativeboard.domain.canvas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aspero.collaborativeboard.domain.canvas.dto.CanvasDTO;
import com.aspero.collaborativeboard.domain.canvas.service.CanvasService;

import java.util.List;

@RestController
@RequestMapping("/api/canvases")
public class CanvasController {

    private final CanvasService canvasService;

    public CanvasController(CanvasService canvasService) {
        this.canvasService = canvasService;
    }

    @GetMapping
    public ResponseEntity<List<CanvasDTO>> getAllCanvases() {
        return ResponseEntity.ok(canvasService.getAllCanvasesForCurrentUser());
    }

    @PostMapping
    public ResponseEntity<CanvasDTO> createCanvas(@RequestBody CanvasDTO canvasDTO) {
        return ResponseEntity.ok(canvasService.createCanvas(canvasDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CanvasDTO> updateCanvas(@PathVariable String id, @RequestBody CanvasDTO canvasDTO) {
        return ResponseEntity.ok(canvasService.updateCanvas(id, canvasDTO));
    }
}