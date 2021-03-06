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

    private Integer nit;

    private String description;

    private String state;

    private String categoryService;
}
