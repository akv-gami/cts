package com.cts.cts.service;

import com.cts.cts.dto.PaymentStatusDto;
import com.cts.cts.exception.AccessDeniedException;
import com.cts.cts.exception.NotFoundException;
import com.cts.cts.model.*;
import com.cts.cts.repository.LlcRepository;
import com.cts.cts.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final LlcRepository llcRepository;
    private final FlowService flowService;

    @Value("${flow.service-price:150000}")
    private int servicePrice;

    public PaymentService(PaymentRepository paymentRepository, LlcRepository llcRepository, FlowService flowService) {
        this.paymentRepository = paymentRepository;
        this.llcRepository = llcRepository;
        this.flowService = flowService;
    }

    private Long getCurrentUserId() {
        UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    private String getCurrentUserEmail() {
        UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getEmail();
    }

    @Transactional
    public String initiatePayment(Long llcId) {
        Long userId = getCurrentUserId();

        LlcEntity llc = llcRepository.findById(llcId)
                .orElseThrow(() -> new NotFoundException("LLC no encontrada"));

        if (!llc.getUserId().equals(userId)) {
            throw new AccessDeniedException("No tienes permiso para pagar esta LLC");
        }

        if (paymentRepository.existsByLlcIdAndStatus(llcId, PaymentStatus.PAID)) {
            throw new IllegalArgumentException("Esta LLC ya tiene un pago confirmado");
        }

        String commerceOrder = "CTS-" + llcId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String subject = "Constitución LLC: " + llc.getBusinessName();
        String email = getCurrentUserEmail();

        FlowService.FlowPaymentResult result = flowService.createPayment(commerceOrder, subject, servicePrice, email);

        PaymentEntity payment = new PaymentEntity();
        payment.setCommerceOrder(commerceOrder);
        payment.setFlowToken(result.token());
        payment.setFlowOrder(result.flowOrder());
        payment.setLlcId(llcId);
        payment.setUserId(userId);
        payment.setAmount(servicePrice);
        payment.setEmail(email);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Pago iniciado para LLC {} con commerceOrder {}", llcId, commerceOrder);
        return result.url();
    }

    @Transactional
    public void processConfirmation(String token) {
        PaymentEntity payment = paymentRepository.findByFlowToken(token).orElse(null);
        if (payment == null) {
            log.warn("Confirmación recibida para token desconocido: {}", token);
            return;
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Pago {} ya estaba confirmado, ignorando", payment.getCommerceOrder());
            return;
        }

        try {
            FlowService.FlowStatusResult status = flowService.getPaymentStatus(token);

            switch (status.status()) {
                case 2 -> {
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    llcRepository.findById(payment.getLlcId()).ifPresent(llc -> {
                        llc.setStatus(LlcStatus.ACTIVE);
                        llcRepository.save(llc);
                        log.info("LLC {} activada tras pago confirmado", llc.getBusinessName());
                    });
                }
                case 3 -> {
                    payment.setStatus(PaymentStatus.REJECTED);
                    paymentRepository.save(payment);
                    log.info("Pago {} rechazado por Flow", payment.getCommerceOrder());
                }
                case 4 -> {
                    payment.setStatus(PaymentStatus.CANCELLED);
                    paymentRepository.save(payment);
                    log.info("Pago {} cancelado", payment.getCommerceOrder());
                }
                default -> log.info("Pago {} en estado pendiente ({})", payment.getCommerceOrder(), status.status());
            }
        } catch (Exception e) {
            log.error("Error al procesar confirmación de pago para token {}: {}", token, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentStatusDto> getMyPayments() {
        Long userId = getCurrentUserId();
        return paymentRepository.findByUserId(userId).stream()
                .map(p -> new PaymentStatusDto(
                        p.getLlcId(),
                        p.getCommerceOrder(),
                        p.getStatus().name(),
                        p.getAmount(),
                        p.getCreatedAt(),
                        p.getPaidAt()
                )).toList();
    }
}