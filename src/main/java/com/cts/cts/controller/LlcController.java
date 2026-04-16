package com.cts.cts.controller;

import com.cts.cts.dto.LlcRequestDto;
import com.cts.cts.dto.LlcResponseDto;
import com.cts.cts.service.TaxLogicService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/llc")
public class LlcController {

    private final TaxLogicService taxLogicService;

    public LlcController(TaxLogicService taxLogicService) {
        this.taxLogicService = taxLogicService;
    }

    @PostMapping("/onboarding")
    public ResponseEntity<LlcResponseDto> createLlc(@Valid @RequestBody LlcRequestDto request) {
        return ResponseEntity.ok(taxLogicService.processNewLlc(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<LlcResponseDto>> getAllLlcs() {
        return ResponseEntity.ok(taxLogicService.getAllLlcs());
    }
}