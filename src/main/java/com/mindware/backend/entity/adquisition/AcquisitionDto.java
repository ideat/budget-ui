package com.mindware.backend.entity.adquisition;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AcquisitionDto {

    private UUID id;

    private String acquisitionNumber;

    private String applicant;

    private String areaApplicant;

    private String typeRequest;

    private String items;

    private String supplier;

    private LocalDate receptionDate;

    private String state;

    private String nameBusinessUnit;

    private Integer codeFatherBusinessUnit;

    private LocalDate dateDeliveryAccounting;

    private String accountingPerson;
}
