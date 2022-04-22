package com.mindware.backend.entity.recurrentService;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class RecurrentServiceDto {

    private UUID id;

    private String typeService;

    private UUID idSupplier;

    private String supplierName;

    private String supplierNit;

    private String supplierLocation;

    private String description;

    private String period;

    private LocalDate paymentDate;

    private String currency;

    private Double amount;

    private String account;

    private String subAccount;

    private String expenseDistribuite; //json

    private String typeDocumentReceived; //factura recibo

    private Integer numberDocumentReceived;

    private Integer numberContract;

    private LocalDate finishDate;

    private Boolean tacitReductionClause;

    private String paymentFrecuency;
}
