package com.agri.silo.service;

import com.agri.silo.dto.SiloCreateRequest;
import com.agri.silo.dto.SiloDTO;
import com.agri.silo.entity.SiloUnit;
import com.agri.silo.enums.FanState;
import com.agri.silo.exception.ResourceNotFoundException;
import com.agri.silo.repository.SiloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiloService {

    private final SiloRepository siloRepository;

    public SiloService(SiloRepository siloRepository) {
        this.siloRepository = siloRepository;
    }

    @Transactional(readOnly = true)
    public List<SiloDTO> getAllSilos() {
        return siloRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SiloDTO getSiloById(Long id) {
        SiloUnit silo = siloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silo not found with id: " + id));
        return mapToDTO(silo);
    }

    @Transactional
    public SiloDTO createSilo(SiloCreateRequest request) {
        SiloUnit silo = SiloUnit.builder()
                .code(request.getCode())
                .name(request.getName())
                .capacityTons(request.getCapacityTons())
                .cropType(request.getCropType())
                .targetMoistureThreshold(request.getTargetMoistureThreshold())
                .idealTempMin(request.getIdealTempMin())
                .idealTempMax(request.getIdealTempMax())
                .fanState(FanState.OFF)
                .build();

        SiloUnit saved = siloRepository.save(silo);
        return mapToDTO(saved);
    }

    @Transactional
    public SiloDTO updateThresholds(Long id, Double targetMoisture, Double idealTempMin, Double idealTempMax) {
        SiloUnit silo = siloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silo not found with id: " + id));

        if (targetMoisture != null) silo.setTargetMoistureThreshold(targetMoisture);
        if (idealTempMin != null) silo.setIdealTempMin(idealTempMin);
        if (idealTempMax != null) silo.setIdealTempMax(idealTempMax);

        SiloUnit saved = siloRepository.save(silo);
        return mapToDTO(saved);
    }

    public SiloDTO mapToDTO(SiloUnit silo) {
        return SiloDTO.builder()
                .id(silo.getId())
                .code(silo.getCode())
                .name(silo.getName())
                .capacityTons(silo.getCapacityTons())
                .cropType(silo.getCropType())
                .targetMoistureThreshold(silo.getTargetMoistureThreshold())
                .idealTempMin(silo.getIdealTempMin())
                .idealTempMax(silo.getIdealTempMax())
                .fanState(silo.getFanState())
                .lastTelemetryAt(silo.getLastTelemetryAt())
                .build();
    }
}
