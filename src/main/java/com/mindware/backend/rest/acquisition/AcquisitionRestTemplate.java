package com.mindware.backend.rest.acquisition;

import com.mindware.backend.entity.adquisition.Acquisition;
import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}
