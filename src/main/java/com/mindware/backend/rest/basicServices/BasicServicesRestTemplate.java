package com.mindware.backend.rest.basicServices;

import com.mindware.backend.entity.basicServices.BasicServices;
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
public class BasicServicesRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public BasicServices add(BasicServices basicServices){
        final String uri = url + "/v1/basicservices/add";
        HttpEntity<BasicServices> entity = new HttpEntity<>(basicServices,HeaderJwt.getHeader());
        ResponseEntity<BasicServices> response = restTemplate.postForEntity(uri,entity,BasicServices.class);
        return response.getBody();
    }

    public void updateState (BasicServices basicServices){
        final String uri = url + "/v1/basicservices/updateState";
        HttpEntity<BasicServices> entity = new HttpEntity<>(basicServices,HeaderJwt.getHeader());
        restTemplate.exchange(uri, HttpMethod.PUT,entity,BasicServices.class);

    }

    public BasicServices getById(String id){
        final String uri = url + "/v1/basicservices/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);
        HttpEntity<BasicServices> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<BasicServices> response = restTemplate.exchange(uri, HttpMethod.GET,entity,BasicServices.class,params);
        return response.getBody();
    }
}
