package com.mindware.backend.entity.adquisition;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Reception {

    private LocalDate dateReception;

    private String receivedBy;
}
