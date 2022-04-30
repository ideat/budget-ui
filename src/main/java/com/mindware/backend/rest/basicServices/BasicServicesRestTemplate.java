package com.mindware.backend.rest.basicServices;

import com.mindware.backend.entity.basicServices.BasicServices;
import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}
