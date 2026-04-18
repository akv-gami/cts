package com.cts.cts.dto;

import java.time.LocalDateTime;

public record PaymentStatusDto(
    Long llcId,
    String commerceOrder,
    String status,
    Integer amount,
    LocalDateTime createdAt,
    LocalDateTime paidAt
) {}