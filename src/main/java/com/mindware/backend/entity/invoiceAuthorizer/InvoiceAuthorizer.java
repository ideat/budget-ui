package com.mindware.backend.entity.invoiceAuthorizer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InvoiceAuthorizer {

    private UUID id;

    private String email;

    private Integer codeBranchOffice;

    private String nameBranchOffice;

    private String codePosition;

    private String position;

    private String fullName;

    private String state;

    private String priorityLevel;
}
