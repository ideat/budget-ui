package com.mindware.backend.entity.config;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TypeChangeCurrency {

    private UUID id;

    private String name;

    private String currency;

    private Double amountChange;

    private LocalDate validityStart;

}
