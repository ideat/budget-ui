package com.mindware.backend.entity.recurrentService;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseDistribuite {

    private Integer codeBusinessUnit;

    private String nameBusinessUnit;

    private Double amount;

    private String currency;
}
