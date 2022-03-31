package com.mindware.backend.entity.config;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Parameter {

    private UUID id;

    private String category;

    private String value;

    private String details;
}
