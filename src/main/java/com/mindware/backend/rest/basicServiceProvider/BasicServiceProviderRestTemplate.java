package com.mindware.backend.rest.basicServiceProvider;

import com.mindware.backend.entity.config.BasicServiceProvider;
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
public class BasicServiceProviderRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public BasicServiceProvider add(BasicServiceProvider basicServiceProvider){
        final String uri = url + "/v1/basic-service-provider/add";
        HttpEntity<BasicServiceProvider> entity = new HttpEntity<>(basicServiceProvider, HeaderJwt.getHeader());
        ResponseEntity<BasicServiceProvider> response = restTemplate.postForEntity(uri,entity,BasicServiceProvider.class);
        return response.getBody();
    }

    public List<BasicServiceProvider> getAll(){
        final String uri = url + "/v1/basic-service-provider/getAll";
        HttpEntity<BasicServiceProvider[]> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<BasicServiceProvider[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,BasicServiceProvider[].class);
        return Arrays.asList(response.getBody());
    }

    public List<BasicServiceProvider> getAllByCategoryService(String categoryService){
        final String uri = url + "/v1/basic-service-provider/getAllCategoryService/{categoryservice}";

        Map<String,String> params = new HashMap<>();
        params.put("categoryservice", categoryService);

        HttpEntity<BasicServiceProvider[]> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<BasicServiceProvider[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,BasicServiceProvider[].class,params);
        return Arrays.asList(response.getBody());
    }

    public BasicServiceProvider getById(String id){
        final String uri = url + "/v1/basic-service-provider/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id", id);
        HttpEntity<BasicServiceProvider[]> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<BasicServiceProvider> response = restTemplate.exchange(uri,HttpMethod.GET,entity,BasicServiceProvider.class,params);
        return response.getBody();
    }
}
