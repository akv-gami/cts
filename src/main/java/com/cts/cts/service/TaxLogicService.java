package com.cts.cts.service;

import com.cts.cts.dto.LlcRequestDto;
import com.cts.cts.dto.LlcResponseDto;
import com.cts.cts.dto.UpdateLlcStatusDto;
import com.cts.cts.exception.AccessDeniedException;
import com.cts.cts.exception.NotFoundException;
import com.cts.cts.model.LlcEntity;
import com.cts.cts.model.LlcStatus;
import com.cts.cts.model.UserEntity;
import com.cts.cts.repository.LlcRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class TaxLogicService {

    private final LlcRepository llcRepository;

    public TaxLogicService(LlcRepository llcRepository) {
        this.llcRepository = llcRepository;
    }

    private Long getCurrentUserId() {
        UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    public LlcResponseDto processNewLlc(LlcRequestDto request) {
        String normalizedState = capitalizeWords(request.stateOfFormation());
        LlcEntity entity = new LlcEntity();
        entity.setUserId(getCurrentUserId());
        entity.setBusinessName(request.businessName().trim() + " LLC");
        entity.setOwnerName(request.ownerName().trim());
        entity.setOwnerRut(request.ownerRut().trim());
        entity.setOwnerEmail(request.ownerEmail().trim().toLowerCase());
        entity.setStateOfFormation(normalizedState);
        entity.setCreationDate(LocalDate.now());
        entity.setAnnualReportDueDate(calculateAnnualReportDueDate(normalizedState));
        entity.setStatus(LlcStatus.PENDING);
        entity.setRequiresForm5472(!request.hasPhysicalPresenceInUs());
        entity.setEin(null);
        return toResponse(llcRepository.save(entity));
    }

    public List<LlcResponseDto> getAllLlcs() {
        return llcRepository.findByUserId(getCurrentUserId()).stream()
                .map(this::toResponse).toList();
    }

    public List<LlcResponseDto> getAllLlcsAdmin() {
        return llcRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    public LlcResponseDto getLlcById(Long id) {
        LlcEntity entity = llcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LLC no encontrada"));
        if (!entity.getUserId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("No tienes permiso para acceder a este recurso");
        }
        return toResponse(entity);
    }

    public LlcResponseDto getLlcByIdAdmin(Long id) {
        return toResponse(llcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LLC no encontrada")));
    }

    public LlcResponseDto updateLlcStatus(Long id, UpdateLlcStatusDto dto) {
        LlcEntity entity = llcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LLC no encontrada"));
        try {
            entity.setStatus(LlcStatus.valueOf(dto.status().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido. Valores permitidos: PENDING, ACTIVE, COMPLETED");
        }
        if (dto.ein() != null && !dto.ein().isBlank()) {
            entity.setEin(dto.ein().trim());
        }
        return toResponse(llcRepository.save(entity));
    }

    private String capitalizeWords(String text) {
        String[] words = text.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase())
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    private LocalDate calculateAnnualReportDueDate(String normalizedState) {
        int year = LocalDate.now().getYear();
        return switch (normalizedState.toLowerCase()) {
            case "wyoming" -> LocalDate.of(year + 1, 1, 1);
            case "delaware" -> LocalDate.of(year + 1, 3, 1);
            case "florida" -> LocalDate.of(year + 1, 5, 1);
            default -> LocalDate.of(year + 1, 4, 15);
        };
    }

    private LlcResponseDto toResponse(LlcEntity entity) {
        return new LlcResponseDto(
            entity.getId(),
            entity.getBusinessName(),
            entity.getStateOfFormation(),
            entity.getOwnerName(),
            entity.getOwnerRut(),
            entity.getOwnerEmail(),
            entity.getEin(),
            entity.getStatus() != null ? entity.getStatus().name() : null,
            entity.getCreationDate(),
            entity.getAnnualReportDueDate(),
            entity.isRequiresForm5472()
        );
    }
}