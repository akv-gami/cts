package com.cts.cts.controller;

import com.cts.cts.dto.PaymentStatusDto;
import com.cts.cts.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create/{llcId}")
    public ResponseEntity<Map<String, String>> createPayment(@PathVariable Long llcId) {
        String paymentUrl = paymentService.initiatePayment(llcId);
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam("token") String token) {
        log.info("Confirmación de pago recibida de Flow para token: {}...", token.substring(0, Math.min(8, token.length())));
        paymentService.processConfirmation(token);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/mine")
    public ResponseEntity<List<PaymentStatusDto>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }
}