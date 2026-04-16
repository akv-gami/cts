package com.cts.cts.dto;

import java.time.LocalDate;

public record LlcResponseDto(
    Long id,
    String businessName,
    String stateOfFormation,
    String ownerName,
    String ownerRut,
    String ownerEmail,
    String ein,
    String status,
    LocalDate creationDate,
    LocalDate annualReportDueDate,
    boolean requiresForm5472
) {}