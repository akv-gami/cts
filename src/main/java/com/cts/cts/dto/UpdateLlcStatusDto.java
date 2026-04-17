package com.cts.cts.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateLlcStatusDto(
    @NotNull(message = "El estado es obligatorio")
    String status,
    String ein
) {}