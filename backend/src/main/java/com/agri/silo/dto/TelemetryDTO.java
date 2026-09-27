package com.agri.silo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryDTO {
    private Long id;
    private Long siloId;
    private String siloCode;
    private String siloName;
    private Double moisturePercent;
    private Double temperatureCelsius;
    private LocalDateTime timestamp;
}
