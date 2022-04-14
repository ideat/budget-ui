package com.mindware.backend.rest.authentication;

import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class AuthenticateRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    @Value("${url_login}")
    private String urlLogin;

    public Token getToken(JwtRequest jwtRequest){
        final String uri = urlLogin + "/ldap/login";
        HttpEntity<JwtRequest> entity = new HttpEntity<>(jwtRequest);
        ResponseEntity<Token> response = restTemplate.postForEntity(uri,entity,Token.class);
        return response.getBody();
    }

    public UserData getDataUser(JwtRequest jwtRequest){
        final String uri = urlLogin + "/ldap/datauser";
        HttpEntity<JwtRequest> entity = new HttpEntity<>(jwtRequest, HeaderJwt.getHeader());
        ResponseEntity<UserData> response = restTemplate.postForEntity(uri,entity,UserData.class);
        return response.getBody();
    }
}
