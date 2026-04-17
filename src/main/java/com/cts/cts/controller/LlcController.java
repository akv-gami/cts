package com.cts.cts.controller;

import com.cts.cts.dto.LlcRequestDto;
import com.cts.cts.dto.LlcResponseDto;
import com.cts.cts.service.PdfService;
import com.cts.cts.service.TaxLogicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/llc")
public class LlcController {

    private final TaxLogicService taxLogicService;
    private final PdfService pdfService;

    public LlcController(TaxLogicService taxLogicService, PdfService pdfService) {
        this.taxLogicService = taxLogicService;
        this.pdfService = pdfService;
    }

    @PostMapping("/onboarding")
    public ResponseEntity<LlcResponseDto> createLlc(@Valid @RequestBody LlcRequestDto request) {
        return ResponseEntity.ok(taxLogicService.processNewLlc(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<LlcResponseDto>> getAllLlcs() {
        return ResponseEntity.ok(taxLogicService.getAllLlcs());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLlc(@PathVariable Long id) {
        taxLogicService.deleteLlc(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        LlcResponseDto llc = taxLogicService.getLlcById(id);
        byte[] pdfBytes = pdfService.generateOperatingAgreement(llc);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                llc.businessName().replace(" ", "_") + "_Operating_Agreement.pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}