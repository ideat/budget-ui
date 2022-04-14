package com.mindware.backend.util;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HeaderJwt {
    public static HttpHeaders getHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+ VaadinSession.getCurrent().getAttribute("jwt"));
        return headers;
    }
}
