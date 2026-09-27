package com.agri.silo.dto;

import com.agri.silo.enums.FanState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiloDTO {
    private Long id;
    private String code;
    private String name;
    private Double capacityTons;
    private String cropType;
    private Double targetMoistureThreshold;
    private Double idealTempMin;
    private Double idealTempMax;
    private FanState fanState;
    private LocalDateTime lastTelemetryAt;
}
