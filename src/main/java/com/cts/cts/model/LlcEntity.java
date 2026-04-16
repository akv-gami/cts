package com.cts.cts.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "llc_companies")
@Getter
@Setter
@NoArgsConstructor
public class LlcEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessName;
    private String stateOfFormation;
    private String ownerName;
    private String ownerRut;
    private String ownerEmail;
    private String ein;
    private String status;
    private LocalDate creationDate;
    private LocalDate annualReportDueDate;
    private boolean requiresForm5472;
}