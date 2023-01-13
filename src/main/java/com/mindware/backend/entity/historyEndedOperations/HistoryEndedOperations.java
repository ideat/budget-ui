package com.mindware.backend.entity.historyEndedOperations;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class HistoryEndedOperations {

    private UUID id;

    private UUID idOperation;

    private String typeEntity;

    private LocalDate dateChanged;

    private String changedBy;

    private String originalDataEntity; //json

    private String changedDataEntity; //json
}
