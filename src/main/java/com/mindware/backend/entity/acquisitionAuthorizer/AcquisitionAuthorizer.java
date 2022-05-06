package com.mindware.backend.entity.acquisitionAuthorizer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AcquisitionAuthorizer {

    private UUID id;

    private Integer codeBranchOffice;

    private String nameBranchOffice;

    private String codePosition;

    private String position;

    private String fullName;

    private String state;

    private Double maxAmount;

    private String email;

}
