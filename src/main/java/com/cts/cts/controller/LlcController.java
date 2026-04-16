package com.cts.cts.controller;

import com.cts.cts.dto.LlcRequestDto;
import com.cts.cts.model.LlcEntity;
import com.cts.cts.service.TaxLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llc")
public class LlcController {

    private final TaxLogicService taxLogicService;

    public LlcController(TaxLogicService taxLogicService) {
        this.taxLogicService = taxLogicService;
    }

    @PostMapping("/onboarding")
    public ResponseEntity<LlcEntity> createLlc(@RequestBody LlcRequestDto request) {
        LlcEntity processedEntity = taxLogicService.processNewLlc(request);
        return ResponseEntity.ok(processedEntity);
    }
}