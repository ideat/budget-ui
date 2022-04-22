package com.mindware.backend.entity.contract;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class Contract {

    private UUID id;

    private UUID idSupplier;

    private Integer nit;

    private Integer numberContract;

    private LocalDate dateSubscription;

    private String objectContract;

    private Double amount;

    private String currency;

    private LocalDate startDate;

    private LocalDate finishDate;

    private String observation;

    private Boolean physical;

    private Boolean original;

    private Boolean undefinedTime;

    private Boolean tacitReductionClause;

    private String paymentFrecuency;
}
