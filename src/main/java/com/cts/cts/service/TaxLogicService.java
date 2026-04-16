package com.cts.cts.service;

import com.cts.cts.dto.LlcRequestDto;
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

    public LlcEntity processNewLlc(LlcRequestDto request) {
        LlcEntity entity = new LlcEntity();
        entity.setBusinessName(request.businessName() + " LLC");
        entity.setOwnerRut(request.ownerRut());
        
        String state = request.stateOfFormation();
        if (state != null && !state.trim().isEmpty()) {
            String[] words = state.trim().split("\\s+");
            StringBuilder capitalized = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    capitalized.append(word.substring(0, 1).toUpperCase())
                                .append(word.substring(1).toLowerCase())
                                .append(" ");
                }
            }
            entity.setStateOfFormation(capitalized.toString().trim());
        } else {
            entity.setStateOfFormation(state);
        }
        
        entity.setCreationDate(LocalDate.now());
        entity.setStatus("PENDING");
        entity.setRequiresForm5472(true);
        return llcRepository.save(entity);
    }

    public List<LlcEntity> getAllLlcs() {
        return llcRepository.findAll();
    }
}