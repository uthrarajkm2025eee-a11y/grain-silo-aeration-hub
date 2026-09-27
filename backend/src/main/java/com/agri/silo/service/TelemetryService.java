package com.agri.silo.service;

import com.agri.silo.dto.*;
import com.agri.silo.entity.FanEvent;
import com.agri.silo.entity.SiloTelemetry;
import com.agri.silo.entity.SiloUnit;
import com.agri.silo.enums.FanAction;
import com.agri.silo.enums.FanState;
import com.agri.silo.enums.TriggeredBy;
import com.agri.silo.exception.ResourceNotFoundException;
import com.agri.silo.repository.FanEventRepository;
import com.agri.silo.repository.SiloRepository;
import com.agri.silo.repository.TelemetryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final SiloRepository siloRepository;
    private final FanEventRepository fanEventRepository;
    private final AerationAutomationService aerationAutomationService;

    public TelemetryService(TelemetryRepository telemetryRepository,
                            SiloRepository siloRepository,
                            FanEventRepository fanEventRepository,
                            AerationAutomationService aerationAutomationService) {
        this.telemetryRepository = telemetryRepository;
        this.siloRepository = siloRepository;
        this.fanEventRepository = fanEventRepository;
        this.aerationAutomationService = aerationAutomationService;
    }

    @Transactional
    public TelemetryDTO ingestTelemetry(TelemetryIngestRequest request) {
        SiloUnit silo = siloRepository.findById(request.getSiloId())
                .orElseThrow(() -> new ResourceNotFoundException("Silo not found with id: " + request.getSiloId()));

        LocalDateTime ts = request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now();

        SiloTelemetry telemetry = SiloTelemetry.builder()
                .silo(silo)
                .moisturePercent(request.getMoisturePercent())
                .temperatureCelsius(request.getTemperatureCelsius())
                .timestamp(ts)
                .build();

        SiloTelemetry saved = telemetryRepository.save(telemetry);

        silo.setLastTelemetryAt(ts);
        siloRepository.save(silo);

        // Synchronous rule evaluation
        aerationAutomationService.evaluateTelemetryRules(saved);

        return mapToTelemetryDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<TelemetryDTO> getSiloTelemetryPaged(Long siloId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SiloTelemetry> telemetryPage;
        if (siloId != null) {
            telemetryPage = telemetryRepository.findBySiloIdOrderByTimestampDesc(siloId, pageable);
        } else {
            telemetryPage = telemetryRepository.findAllPaged(pageable);
        }
        return telemetryPage.map(this::mapToTelemetryDTO);
    }

    @Transactional(readOnly = true)
    public List<FanEventDTO> getFanEvents(Long siloId) {
        List<FanEvent> events;
        if (siloId != null) {
            events = fanEventRepository.findBySiloIdOrderByTimestampDesc(siloId);
        } else {
            events = fanEventRepository.findAll();
        }
        return events.stream().map(this::mapToFanEventDTO).collect(Collectors.toList());
    }

    @Transactional
    public FanEventDTO manualFanOverride(FanOverrideRequest request) {
        SiloUnit silo = siloRepository.findById(request.getSiloId())
                .orElseThrow(() -> new ResourceNotFoundException("Silo not found with id: " + request.getSiloId()));

        FanState newState = request.getAction() == FanAction.ON ? FanState.ON : FanState.OFF;
        silo.setFanState(newState);
        siloRepository.save(silo);

        String reason = request.getReason() != null && !request.getReason().isBlank() ?
                request.getReason() : "Manual override by Operator";

        FanEvent event = FanEvent.builder()
                .silo(silo)
                .action(request.getAction())
                .triggeredBy(TriggeredBy.MANUAL)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();

        FanEvent saved = fanEventRepository.save(event);
        return mapToFanEventDTO(saved);
    }

    private TelemetryDTO mapToTelemetryDTO(SiloTelemetry t) {
        return TelemetryDTO.builder()
                .id(t.getId())
                .siloId(t.getSilo().getId())
                .siloCode(t.getSilo().getCode())
                .siloName(t.getSilo().getName())
                .moisturePercent(t.getMoisturePercent())
                .temperatureCelsius(t.getTemperatureCelsius())
                .timestamp(t.getTimestamp())
                .build();
    }

    private FanEventDTO mapToFanEventDTO(FanEvent e) {
        return FanEventDTO.builder()
                .id(e.getId())
                .siloId(e.getSilo().getId())
                .siloCode(e.getSilo().getCode())
                .siloName(e.getSilo().getName())
                .action(e.getAction())
                .triggeredBy(e.getTriggeredBy())
                .telemetryId(e.getTelemetryId())
                .reason(e.getReason())
                .timestamp(e.getTimestamp())
                .build();
    }
}
