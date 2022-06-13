package com.mindware.backend.entity.supplier;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Supplier {

    private UUID id;

    private String nit;

    private String name;

    private String legalRepresentative;

    private String idCardLegalRepresentative;

    private String areaWork; //rubro

    private String typeBusinessCompany;//srl, sa, unipersonal

    private String primaryActivity;

    private String email;

    private String address;

    private String phoneNumber;

    private String location;

    private String shareHolders; //json

    private String pendingCompleting;
}
