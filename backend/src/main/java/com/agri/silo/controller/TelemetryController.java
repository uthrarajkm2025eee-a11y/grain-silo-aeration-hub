package com.agri.silo.controller;

import com.agri.silo.dto.TelemetryDTO;
import com.agri.silo.dto.TelemetryIngestRequest;
import com.agri.silo.service.TelemetryService;
import com.agri.silo.service.TelemetrySimulatorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/silos")
public class TelemetryController {

    private final TelemetryService telemetryService;
    private final TelemetrySimulatorService simulatorService;

    public TelemetryController(TelemetryService telemetryService, TelemetrySimulatorService simulatorService) {
        this.telemetryService = telemetryService;
        this.simulatorService = simulatorService;
    }

    @PostMapping("/telemetry")
    public ResponseEntity<TelemetryDTO> ingestTelemetry(@Valid @RequestBody TelemetryIngestRequest request) {
        TelemetryDTO ingested = telemetryService.ingestTelemetry(request);
        return new ResponseEntity<>(ingested, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/telemetry")
    public ResponseEntity<Page<TelemetryDTO>> getSiloTelemetry(@PathVariable("id") Long siloId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(telemetryService.getSiloTelemetryPaged(siloId, page, size));
    }

    @GetMapping("/telemetry/all")
    public ResponseEntity<Page<TelemetryDTO>> getAllTelemetry(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(telemetryService.getSiloTelemetryPaged(null, page, size));
    }

    @PostMapping("/telemetry/simulator/toggle")
    public ResponseEntity<Map<String, Object>> toggleSimulator(@RequestParam(required = false) Boolean enabled) {
        boolean nextState = (enabled != null) ? enabled : !simulatorService.isSimulatorEnabled();
        simulatorService.setSimulatorEnabled(nextState);
        return ResponseEntity.ok(Map.of(
                "simulatorEnabled", nextState,
                "message", nextState ? "Telemetry simulator started" : "Telemetry simulator stopped"
        ));
    }

    @GetMapping("/telemetry/simulator/status")
    public ResponseEntity<Map<String, Object>> getSimulatorStatus() {
        return ResponseEntity.ok(Map.of("simulatorEnabled", simulatorService.isSimulatorEnabled()));
    }
}
