package com.agri.silo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SiloCreateRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be positive")
    private Double capacityTons;

    @NotBlank(message = "Crop type is required")
    private String cropType;

    private Double targetMoistureThreshold = 14.5;
    private Double idealTempMin = 10.0;
    private Double idealTempMax = 25.0;
}
