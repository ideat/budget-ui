package com.mindware.backend.rest.period;

import com.mindware.backend.entity.config.Period;
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
public class PeriodRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public Period add(Period period){
        final String uri = url + "/v1/period/add";
        HttpEntity<Period> entity = new HttpEntity<>(period, HeaderJwt.getHeader());
        ResponseEntity<Period> response = restTemplate.postForEntity(uri,entity,Period.class);
        return response.getBody();
    }

    public List<Period> getAll(){
        final String uri = url + "/v1/period/getAll";

        HttpEntity<Period[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<Period[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Period[].class);
        return Arrays.asList(response.getBody());
    }


}
