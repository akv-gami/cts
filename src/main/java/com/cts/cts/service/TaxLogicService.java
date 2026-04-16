package com.cts.cts.service;

import com.cts.cts.dto.LlcRequestDto;
import com.cts.cts.dto.LlcResponseDto;
import com.cts.cts.model.LlcEntity;
import com.cts.cts.repository.LlcRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class TaxLogicService {

    private final LlcRepository llcRepository;

    public TaxLogicService(LlcRepository llcRepository) {
        this.llcRepository = llcRepository;
    }

    public LlcResponseDto processNewLlc(LlcRequestDto request) {
        LlcEntity entity = new LlcEntity();
        entity.setBusinessName(request.businessName().trim() + " LLC");
        entity.setOwnerName(request.ownerName().trim());
        entity.setOwnerRut(request.ownerRut().trim());
        entity.setOwnerEmail(request.ownerEmail().trim().toLowerCase());
        entity.setStateOfFormation(capitalizeWords(request.stateOfFormation()));
        entity.setCreationDate(LocalDate.now());
        entity.setAnnualReportDueDate(calculateAnnualReportDueDate(request.stateOfFormation()));
        entity.setStatus("PENDING");
        entity.setRequiresForm5472(!request.hasPhysicalPresenceInUs());
        entity.setEin(null);

        LlcEntity saved = llcRepository.save(entity);
        return toResponse(saved);
    }

    public List<LlcResponseDto> getAllLlcs() {
        return llcRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
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

    private LocalDate calculateAnnualReportDueDate(String state) {
        int currentYear = LocalDate.now().getYear();
        return switch (state.trim().toLowerCase()) {
            case "wyoming" -> LocalDate.of(currentYear + 1, 1, 1);
            case "delaware" -> LocalDate.of(currentYear + 1, 3, 1);
            case "florida" -> LocalDate.of(currentYear + 1, 5, 1);
            default -> LocalDate.of(currentYear + 1, 4, 15);
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
            entity.getStatus(),
            entity.getCreationDate(),
            entity.getAnnualReportDueDate(),
            entity.isRequiresForm5472()
        );
    }
}