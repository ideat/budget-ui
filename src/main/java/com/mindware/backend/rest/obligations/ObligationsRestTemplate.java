package com.mindware.backend.rest.obligations;

import com.mindware.backend.entity.obligations.Obligations;
import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ObligationsRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public Obligations add(Obligations obligations){
        final String uri = url + "/v1/obligations/add";
        HttpEntity<Obligations> entity = new HttpEntity<>(obligations, HeaderJwt.getHeader());
        ResponseEntity<Obligations> response = restTemplate.postForEntity(uri,entity,Obligations.class);
        return response.getBody();
    }

    public void updateSate(Obligations obligations){
        final String uri = url + "/v1/obligations/updateState";
        HttpEntity<Obligations> entity = new HttpEntity<>(obligations,HeaderJwt.getHeader());
        restTemplate.exchange(uri, HttpMethod.PUT,entity,Obligations.class);
    }

    public Obligations getById(String id){
        final String uri = url + "/v1/obligations/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);
        HttpEntity<Obligations> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<Obligations> response = restTemplate.exchange(uri,HttpMethod.GET,entity,Obligations.class,params);
        return response.getBody();
    }

}
