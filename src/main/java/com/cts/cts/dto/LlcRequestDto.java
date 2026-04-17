package com.cts.cts.dto;

import com.cts.cts.validation.ValidRut;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LlcRequestDto(
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    String businessName,

    @NotBlank(message = "El RUT es obligatorio")
    @ValidRut
    String ownerRut,

    @NotBlank(message = "El nombre del titular es obligatorio")
    String ownerName,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    String ownerEmail,

    @NotBlank(message = "El estado de formación es obligatorio")
    String stateOfFormation,

    boolean hasPhysicalPresenceInUs
) {}