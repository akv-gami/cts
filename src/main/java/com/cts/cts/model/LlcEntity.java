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

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String businessName;

    @Column(nullable = false, length = 100)
    private String stateOfFormation;

    @Column(nullable = false, length = 150)
    private String ownerName;

    @Column(nullable = false, length = 20)
    private String ownerRut;

    @Column(nullable = false, length = 200)
    private String ownerEmail;

    @Column(length = 20)
    private String ein;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LlcStatus status;

    @Column(nullable = false)
    private LocalDate creationDate;

    private LocalDate annualReportDueDate;

    @Column(nullable = false)
    private boolean requiresForm5472;
}