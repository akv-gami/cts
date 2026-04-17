package com.cts.cts.controller;

import com.cts.cts.dto.LlcResponseDto;
import com.cts.cts.dto.UpdateLlcStatusDto;
import com.cts.cts.service.PdfService;
import com.cts.cts.service.TaxLogicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final TaxLogicService taxLogicService;
    private final PdfService pdfService;

    public AdminController(TaxLogicService taxLogicService, PdfService pdfService) {
        this.taxLogicService = taxLogicService;
        this.pdfService = pdfService;
    }

    @GetMapping("/llcs")
    public ResponseEntity<List<LlcResponseDto>> getAllLlcs() {
        return ResponseEntity.ok(taxLogicService.getAllLlcsAdmin());
    }

    @PatchMapping("/llcs/{id}/status")
    public ResponseEntity<LlcResponseDto> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateLlcStatusDto dto) {
        return ResponseEntity.ok(taxLogicService.updateLlcStatus(id, dto));
    }

    @GetMapping("/llcs/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        LlcResponseDto llc = taxLogicService.getLlcByIdAdmin(id);
        byte[] pdfBytes = pdfService.generateOperatingAgreement(llc);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", llc.businessName().replace(" ", "_") + "_OA.pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}