package com.agri.silo.entity;

import com.agri.silo.enums.FanState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "silos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiloUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "capacity_tons", nullable = false)
    private Double capacityTons;

    @Column(name = "crop_type", nullable = false, length = 50)
    private String cropType;

    @Column(name = "target_moisture_threshold", nullable = false)
    private Double targetMoistureThreshold;

    @Column(name = "ideal_temp_min", nullable = false)
    private Double idealTempMin;

    @Column(name = "ideal_temp_max", nullable = false)
    private Double idealTempMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "fan_state", nullable = false, length = 10)
    private FanState fanState;

    @Column(name = "last_telemetry_at")
    private LocalDateTime lastTelemetryAt;
}
