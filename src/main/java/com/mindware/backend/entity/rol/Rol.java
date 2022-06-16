package com.mindware.backend.entity.rol;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Rol {

    private UUID id;

    private String name;

    private String options; //json

    private String description;

    private String scope;
}
