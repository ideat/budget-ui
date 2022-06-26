package com.mindware.backend.entity.adquisition;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ExpenseDistribuiteAcquisition {

    private UUID id;

    private String nameItem;

    private Integer quantity;

    private Double amount;

    private String account;

    private String subAccount;

    private Integer codeBusinessUnit;

    private String nameBusinessUnit;

    private Integer codeFatherBusiness;

}
