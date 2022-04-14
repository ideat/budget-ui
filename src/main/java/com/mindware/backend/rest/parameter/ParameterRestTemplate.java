package com.mindware.backend.rest.parameter;

import com.mindware.backend.entity.config.Parameter;
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
public class ParameterRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public Parameter add(Parameter parameter){
        final String uri = url + "/v1/parameter/add";
        HttpEntity<Parameter> entity = new HttpEntity<>(parameter, HeaderJwt.getHeader());
        ResponseEntity<Parameter> response = restTemplate.postForEntity(uri,entity,Parameter.class);
        return response.getBody();
    }

    public List<Parameter> getAll(){
        final String uri = url +"/v1/parameter/getAll";

        HttpEntity<Parameter[]> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<Parameter[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Parameter[].class);

        return Arrays.asList(response.getBody());
    }

    public List<Parameter> getByCategory(String category){
        final String uri = url + "/v1/parameter/getByCategory/{category}";
        Map<String,String> params = new HashMap<>();
        params.put("category",category);

        HttpEntity<Parameter[]> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<Parameter[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity,Parameter[].class,params);
        // Arrays.asList(restTemplate.getForObject(uri,Parameter[].class,params));
        return Arrays.asList(response.getBody());
    }



}
