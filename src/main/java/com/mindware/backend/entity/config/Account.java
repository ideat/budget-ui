package com.mindware.backend.entity.config;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Account {

    private UUID id;

    private String numberAccount;

    private String nameAccount;

    private String subAccount; //json

    private String currency;

    private Integer period;

    private String state;
}
