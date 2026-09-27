package com.agri.silo.dto;

import com.agri.silo.enums.FanAction;
import com.agri.silo.enums.TriggeredBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FanEventDTO {
    private Long id;
    private Long siloId;
    private String siloCode;
    private String siloName;
    private FanAction action;
    private TriggeredBy triggeredBy;
    private Long telemetryId;
    private String reason;
    private LocalDateTime timestamp;
}
