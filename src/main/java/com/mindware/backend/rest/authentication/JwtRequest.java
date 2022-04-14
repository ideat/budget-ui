package com.mindware.backend.rest.authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtRequest {
    private static final long serialVersionUID = 5926468583005150707L;
    private String username;
    private String password;
}
