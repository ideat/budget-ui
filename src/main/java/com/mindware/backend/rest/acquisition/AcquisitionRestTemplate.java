package com.mindware.backend.rest.acquisition;

import com.mindware.backend.entity.adquisition.Acquisition;
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
public class AcquisitionRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public Acquisition add(Acquisition acquisition){
        final String uri = url + "/v1/acquisition/add";
        HttpEntity<Acquisition> entity = new HttpEntity<>(acquisition, HeaderJwt.getHeader());
        ResponseEntity<Acquisition> response = restTemplate.postForEntity(uri,entity,Acquisition.class);
        return response.getBody();
    }

    public Acquisition getById(String id){
        final String uri = url + "/v1/acquisition/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);
        HttpEntity<Acquisition> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<Acquisition> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Acquisition.class,params);
        return response.getBody();
    }
}
