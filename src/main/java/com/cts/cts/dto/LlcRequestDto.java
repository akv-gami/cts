package com.cts.cts.dto;

public record LlcRequestDto(
    String businessName, 
    String ownerRut, 
    String ownerName,
    boolean hasPhysicalPresenceInUs
) {}