package com.mindware.backend.entity.adquisition;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Item {

    private UUID id;

    private Integer quantity;

    private String description;

}
