package com.mindware.backend.rest.contract;

import com.mindware.backend.entity.contract.Contract;
import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public Contract add(Contract contract){
        final String uri = url + "/v1/contract/add";
        HttpEntity<Contract> entity = new HttpEntity<>(contract, HeaderJwt.getHeader());
        ResponseEntity<Contract> response = restTemplate.postForEntity(uri,entity,Contract.class);
        return  response.getBody();
    }

    public List<Contract> getAll(){
        final String uri = url + "/v1/contract/getAll";
        HttpEntity<Contract[]> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<Contract[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Contract[].class);
        return Arrays.asList(response.getBody());
    }

    public Contract getById(String id){
        final String uri = url + "/v1/contract/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id", id);

        HttpEntity<Contract> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<Contract> response = restTemplate.exchange(uri,HttpMethod.GET,entity,Contract.class,params);
        return response.getBody();
    }

    public List<Contract> getByIdSupplier(String idsupplier){
        final String uri = url + "/v1/contract/getByIdSupplier/{idsupplier}";
        Map<String,String> params = new HashMap<>();
        params.put("idsupplier", idsupplier);

        HttpEntity<Contract[]> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<Contract[]> response = restTemplate.exchange(uri,HttpMethod.GET,entity,Contract[].class,params);
        return Arrays.asList(response.getBody());
    }
}
