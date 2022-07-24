package com.mindware.backend.entity.rol;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Option {

    private String name;

    private boolean assigned;

    private boolean write;

    private boolean read;

    private boolean send;

    private boolean observed;

    private boolean finish;

    private boolean accounting;

    private boolean delete;

    private String description;

}
