package com.mindware.backend.entity.config;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubAccount {

    private UUID id;

    private String numberSubAccount;

    private String nameSubAccount;
}
