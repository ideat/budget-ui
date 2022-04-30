package com.mindware.backend.entity.basicServices;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class BasicServicesDto {

    private UUID id;

    private String typeBasicService;

    private UUID idBasicServicesProvider;

    private String nameBasicServiceProvider;

    private String descriptionServiceProvider;

    private String description;

    private LocalDate paymentDate;

    private String period;

    private Double amount;

    private String account;

    private String subAccount;

    private String typeDocumentReceived;

    private Integer numberDocumentReceived;

    private String expenseDistribuite; //json
}
