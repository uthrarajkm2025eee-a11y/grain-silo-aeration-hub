package com.agri.silo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TelemetryIngestRequest {
    @NotNull(message = "Silo ID is required")
    private Long siloId;

    @NotNull(message = "Moisture percent is required")
    private Double moisturePercent;

    @NotNull(message = "Temperature in Celsius is required")
    private Double temperatureCelsius;

    private LocalDateTime timestamp;
}
