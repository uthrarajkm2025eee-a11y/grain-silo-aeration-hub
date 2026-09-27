package com.agri.silo.controller;

import com.agri.silo.dto.SiloCreateRequest;
import com.agri.silo.dto.SiloDTO;
import com.agri.silo.service.SiloService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/silos")
public class SiloController {

    private final SiloService siloService;

    public SiloController(SiloService siloService) {
        this.siloService = siloService;
    }

    @GetMapping("/units")
    public ResponseEntity<List<SiloDTO>> getAllSilos() {
        return ResponseEntity.ok(siloService.getAllSilos());
    }

    @GetMapping("/units/{id}")
    public ResponseEntity<SiloDTO> getSiloById(@PathVariable Long id) {
        return ResponseEntity.ok(siloService.getSiloById(id));
    }

    @PostMapping("/units")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiloDTO> createSilo(@Valid @RequestBody SiloCreateRequest request) {
        SiloDTO created = siloService.createSilo(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/units/{id}/thresholds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiloDTO> updateThresholds(@PathVariable Long id,
                                                    @RequestParam(required = false) Double targetMoisture,
                                                    @RequestParam(required = false) Double idealTempMin,
                                                    @RequestParam(required = false) Double idealTempMax) {
        SiloDTO updated = siloService.updateThresholds(id, targetMoisture, idealTempMin, idealTempMax);
        return ResponseEntity.ok(updated);
    }
}
