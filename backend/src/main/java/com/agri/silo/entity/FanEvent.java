package com.agri.silo.entity;

import com.agri.silo.enums.FanAction;
import com.agri.silo.enums.TriggeredBy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fan_events", indexes = {
    @Index(name = "idx_fanevent_silo_ts", columnList = "silo_id, timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FanEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "silo_id", nullable = false)
    private SiloUnit silo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FanAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false, length = 20)
    private TriggeredBy triggeredBy;

    @Column(name = "telemetry_id")
    private Long telemetryId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
