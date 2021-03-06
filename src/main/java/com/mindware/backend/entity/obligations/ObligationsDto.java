package com.mindware.backend.entity.obligations;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ObligationsDto {

    private UUID id;

    private String typeObligation;

    private UUID idSupplier;

    private String nameSupplier;

    private String nitSupplier;

    private String description;

    private String period;

    private LocalDate paymentDate;

    private Double amount;

    private String account;

    private String subAccount;

    private String expenseDistribuite; //json

    private String typeDocumentReceived;

    private String numberDocumentReceived;

    private String invoiceAuthorizer; //json

    private LocalDate dateDeliveryAccounting;

    private String accountingPerson;

    private String state;

    private String createdBy;
}
