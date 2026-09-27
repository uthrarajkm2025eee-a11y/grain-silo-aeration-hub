package com.agri.silo.dto;

import com.agri.silo.enums.FanAction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FanOverrideRequest {
    @NotNull(message = "Silo ID is required")
    private Long siloId;

    @NotNull(message = "Action is required")
    private FanAction action;

    private String reason;
}
