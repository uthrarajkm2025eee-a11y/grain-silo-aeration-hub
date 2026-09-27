package com.agri.silo.service;

import com.agri.silo.dto.TelemetryIngestRequest;
import com.agri.silo.entity.SiloUnit;
import com.agri.silo.repository.SiloRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@EnableScheduling
public class TelemetrySimulatorService {

    private static final Logger log = LoggerFactory.getLogger(TelemetrySimulatorService.class);

    private final SiloRepository siloRepository;
    private final TelemetryService telemetryService;
    private final Random random = new Random();

    private boolean simulatorEnabled;

    public TelemetrySimulatorService(SiloRepository siloRepository,
                                     TelemetryService telemetryService,
                                     @Value("${app.telemetry.simulator.enabled:true}") boolean simulatorEnabled) {
        this.siloRepository = siloRepository;
        this.telemetryService = telemetryService;
        this.simulatorEnabled = simulatorEnabled;
    }

    public boolean isSimulatorEnabled() {
        return simulatorEnabled;
    }

    public void setSimulatorEnabled(boolean enabled) {
        this.simulatorEnabled = enabled;
        log.info("Telemetry Simulator state changed to: {}", enabled);
    }

    @Scheduled(fixedRateString = "${app.telemetry.simulator.interval-ms:8000}")
    public void generateSimulatedTelemetry() {
        if (!simulatorEnabled) return;

        List<SiloUnit> silos = siloRepository.findAll();
        if (silos.isEmpty()) return;

        for (SiloUnit silo : silos) {
            // Generate plausible fluctuations
            // Moisture base around threshold +/- 2.0%
            double targetThreshold = silo.getTargetMoistureThreshold();
            double moistureDelta = (random.nextDouble() * 4.0) - 2.0; // -2.0 to +2.0
            double moisture = Math.max(10.0, Math.min(22.0, round(targetThreshold + moistureDelta, 1)));

            // Temp base around 18.0°C +/- 8.0°C
            double temp = round(10.0 + random.nextDouble() * 16.0, 1);

            TelemetryIngestRequest request = new TelemetryIngestRequest();
            request.setSiloId(silo.getId());
            request.setMoisturePercent(moisture);
            request.setTemperatureCelsius(temp);
            request.setTimestamp(LocalDateTime.now());

            try {
                telemetryService.ingestTelemetry(request);
            } catch (Exception e) {
                log.error("Failed to ingest simulated telemetry for silo {}: {}", silo.getCode(), e.getMessage());
            }
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
