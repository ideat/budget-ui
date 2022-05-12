package com.mindware.backend.entity.adquisition;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class Acquisition {

    private UUID id;

    private Integer acquisitionNumber;

    private Integer codeBusinessUnit;

    private String nameBusinessUnit;
    
    private String applicant;

    private String areaApplicant;

    private String typeRequest;

    private String items; //json

    private LocalDate receptionDate;

    private LocalDate quotationRequestDate; //solicitud cotizacion

    private LocalDate quotationReceptionDate; //Recepcion cotizacion

    //Approbation board CAABS

    private Integer caabsNumber;

    private String currency;

    private Double amount;

    private String authorizersLevel1; //json

    private String authorizersLevel2; //json
    /////
    private String adjudicationInformation; //json

    private String receptionInformation; //json

    private String invoiceInformation; //json

    private String expenseDistribuite; //json

    private LocalDate dateDeliveryAccounting;

    private String accoutingPerson;

    private LocalDate dateDeliveryAaaf;

    private String state; //INICIADO, ENVIADO, OBSERVADO, POR REGULARIZAR, FINALIZADO

    private UUID idSupplier;

    private Integer invoiceNumber;

    private Boolean isBudgeted; //presupuestado?
}
