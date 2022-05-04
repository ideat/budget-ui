package com.mindware.backend.entity.adquisition;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdjudicationInfomation {

    private LocalDate purchaseOrder;

    private Integer deliveryTime;

    private boolean requiresAdvance;

    private boolean contract;

    private LocalDate requireUpdateDoc;

    private LocalDate contractRequestDateToLegal;

    private LocalDate contractDeliverContractFromLegal;

    private LocalDate dateSignature;

    private String authorizationBy;
}
