package com.mindware.backend.entity.supplier;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ShareHolder {

    private UUID id;

    private String fullName;

    private String idCard;
}
