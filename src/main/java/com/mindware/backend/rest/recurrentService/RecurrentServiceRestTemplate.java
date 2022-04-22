package com.mindware.backend.rest.recurrentService;

import com.mindware.backend.entity.recurrentService.RecurrentService;
import com.mindware.backend.util.HeaderJwt;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecurrentServiceRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public RecurrentService add(RecurrentService recurrentService){
        final String uri = url + "/v1/recurrent-service/add";
        HttpEntity<RecurrentService> entity = new HttpEntity<>(recurrentService, HeaderJwt.getHeader());
        ResponseEntity<RecurrentService> response = restTemplate.postForEntity(uri,entity,RecurrentService.class);
        return response.getBody();
    }

    public List<RecurrentService> getAll(){
        final String uri = url + "/v1/recurrent-service/getAll";
        HttpEntity<RecurrentService[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<RecurrentService[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,RecurrentService[].class);
        return Arrays.asList(response.getBody());
    }

    public RecurrentService  getById(String id){
        final String uri = url + "/v1/recurrent-service/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);
        HttpEntity<RecurrentService> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<RecurrentService> response = restTemplate.exchange(uri, HttpMethod.GET,entity,RecurrentService.class,params);
        return response.getBody();
    }


}
