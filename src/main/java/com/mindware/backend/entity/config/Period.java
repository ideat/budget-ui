package com.mindware.backend.entity.config;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Period {

    private UUID id;

    private Integer year;

    private Boolean isOpen;

}
