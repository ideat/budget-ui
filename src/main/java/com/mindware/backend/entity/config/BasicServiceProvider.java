package com.mindware.backend.entity.config;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BasicServiceProvider {

    private UUID id;

    private String typeService;

    private String provider;

    private String nit;

    private String description;

    private String state;
}
