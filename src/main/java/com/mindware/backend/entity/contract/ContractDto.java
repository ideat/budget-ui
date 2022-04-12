package com.mindware.backend.entity.contract;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ContractDto {

    private UUID idContract;

    private UUID idSupplier;

    private String supplierName;

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
}
