package com.mindware.backend.rest.corebank;

import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ConceptRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public List<Concept> getAgencia(){
        final String uri = url + "/v1/corebank/getConcept/agencias";
        HttpEntity<Concept[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<Concept[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Concept[].class);
        return Arrays.asList(response.getBody());
    }

    public List<Concept> getSucursal(){
        final String uri = url + "/v1/corebank/getConcept/sucursal";
        HttpEntity<Concept[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<Concept[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Concept[].class);
        return Arrays.asList(response.getBody());
    }
}
