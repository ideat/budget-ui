package com.mindware.backend.rest.supplier;

import com.mindware.backend.entity.supplier.Supplier;
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
public class SupplierRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public Supplier add(Supplier supplier){
        final String uri = url + "/v1/supplier/add";
        HttpEntity<Supplier> entity = new HttpEntity<>(supplier);
        ResponseEntity<Supplier> response = restTemplate.postForEntity(uri,entity,Supplier.class);
        return response.getBody();
    }

    public List<Supplier> getAll(){
        final String uri = url + "/v1/supplier/getAll";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Supplier[]> entity = new HttpEntity<>(headers);
        ResponseEntity<Supplier[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Supplier[].class);

        return Arrays.asList(response.getBody());
    }

    public List<Supplier> getByLocation(String location){
        final String uri = url + "/v1/supplier/getByLocation/{location}";
        Map<String,String> params = new HashMap<>();
        params.put("location",location);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Supplier[]> entity = new HttpEntity<>(headers);
        ResponseEntity<Supplier[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Supplier[].class,params);
        return Arrays.asList(response.getBody());

    }
}
