package com.agri.silo.controller;

import com.agri.silo.dto.FanEventDTO;
import com.agri.silo.dto.FanOverrideRequest;
import com.agri.silo.service.TelemetryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fan-events")
public class FanEventController {

    private final TelemetryService telemetryService;

    public FanEventController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping
    public ResponseEntity<List<FanEventDTO>> getFanEvents(@RequestParam(required = false) Long siloId) {
        return ResponseEntity.ok(telemetryService.getFanEvents(siloId));
    }

    @PostMapping("/override")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FanEventDTO> manualOverride(@Valid @RequestBody FanOverrideRequest request) {
        FanEventDTO event = telemetryService.manualFanOverride(request);
        return ResponseEntity.ok(event);
    }
}
