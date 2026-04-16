package com.cts.cts.service;

import com.cts.cts.dto.LlcRequestDto;
import com.cts.cts.model.LlcEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class TaxLogicService {

    public LlcEntity processNewLlc(LlcRequestDto request) {
        LlcEntity entity = new LlcEntity();
        entity.setBusinessName(request.businessName() + " LLC");
        entity.setOwnerRut(request.ownerRut());
        entity.setCreationDate(LocalDate.now());
        entity.setStatus("PENDING");
        entity.setRequiresForm5472(true);
        return entity;
    }
}