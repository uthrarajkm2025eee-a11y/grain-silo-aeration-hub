package com.agri.silo.service;

import com.agri.silo.entity.FanEvent;
import com.agri.silo.entity.SiloTelemetry;
import com.agri.silo.entity.SiloUnit;
import com.agri.silo.enums.FanAction;
import com.agri.silo.enums.FanState;
import com.agri.silo.enums.TriggeredBy;
import com.agri.silo.repository.FanEventRepository;
import com.agri.silo.repository.SiloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AerationAutomationService {

    private final SiloRepository siloRepository;
    private final FanEventRepository fanEventRepository;

    public AerationAutomationService(SiloRepository siloRepository, FanEventRepository fanEventRepository) {
        this.siloRepository = siloRepository;
        this.fanEventRepository = fanEventRepository;
    }

    @Transactional
    public void evaluateTelemetryRules(SiloTelemetry telemetry) {
        SiloUnit silo = telemetry.getSilo();

        double moisture = telemetry.getMoisturePercent();
        double temp = telemetry.getTemperatureCelsius();
        double threshold = silo.getTargetMoistureThreshold();
        double minTemp = silo.getIdealTempMin();
        double maxTemp = silo.getIdealTempMax();

        boolean moistureHigh = moisture > threshold;
        boolean tempInWindow = temp >= minTemp && temp <= maxTemp;

        if (moistureHigh && tempInWindow) {
            if (silo.getFanState() == FanState.OFF) {
                silo.setFanState(FanState.ON);
                siloRepository.save(silo);

                String reason = String.format("Moisture (%.1f%%) > threshold (%.1f%%) & Temp (%.1f°C) within window [%.1f - %.1f°C]",
                        moisture, threshold, temp, minTemp, maxTemp);

                FanEvent event = FanEvent.builder()
                        .silo(silo)
                        .action(FanAction.ON)
                        .triggeredBy(TriggeredBy.SYSTEM)
                        .telemetryId(telemetry.getId())
                        .reason(reason)
                        .timestamp(LocalDateTime.now())
                        .build();

                fanEventRepository.save(event);
            }
        } else if (silo.getFanState() == FanState.ON) {
            // Turn fan OFF if conditions no longer met
            String reason;
            if (!moistureHigh && !tempInWindow) {
                reason = String.format("Moisture (%.1f%%) <= threshold (%.1f%%) and Temp (%.1f°C) outside window", moisture, threshold, temp);
            } else if (!moistureHigh) {
                reason = String.format("Moisture target reached (%.1f%% <= %.1f%%)", moisture, threshold);
            } else {
                reason = String.format("Temperature (%.1f°C) outside safe window [%.1f - %.1f°C]", temp, minTemp, maxTemp);
            }

            silo.setFanState(FanState.OFF);
            siloRepository.save(silo);

            FanEvent event = FanEvent.builder()
                    .silo(silo)
                    .action(FanAction.OFF)
                    .triggeredBy(TriggeredBy.SYSTEM)
                    .telemetryId(telemetry.getId())
                    .reason(reason)
                    .timestamp(LocalDateTime.now())
                    .build();

            fanEventRepository.save(event);
        }
    }
}
