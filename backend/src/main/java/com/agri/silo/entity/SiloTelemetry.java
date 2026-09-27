package com.agri.silo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "silo_telemetry", indexes = {
    @Index(name = "idx_telemetry_silo_ts", columnList = "silo_id, timestamp DESC"),
    @Index(name = "idx_telemetry_ts", columnList = "timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiloTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "silo_id", nullable = false)
    private SiloUnit silo;

    @Column(name = "moisture_percent", nullable = false)
    private Double moisturePercent;

    @Column(name = "temperature_celsius", nullable = false)
    private Double temperatureCelsius;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
