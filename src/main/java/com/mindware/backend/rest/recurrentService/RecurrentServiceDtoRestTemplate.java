package com.mindware.backend.rest.recurrentService;

import com.mindware.backend.entity.recurrentService.RecurrentServiceDto;
import com.mindware.backend.util.HeaderJwt;
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
public class RecurrentServiceDtoRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public List<RecurrentServiceDto> getAll(){
        final String uri = url + "/v1/recurrent-service-dto/getAll";
        HttpEntity<RecurrentServiceDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<RecurrentServiceDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,RecurrentServiceDto[].class);
        return Arrays.asList(response.getBody());
    }

    public List<RecurrentServiceDto> getBySupplierLocation(String location){
        final String uri = url + "/v1/recurrent-service-dto/getBySupplierLocation/{location}";
        Map<String,String> params = new HashMap<>();
        params.put("location",location);

        HttpEntity<RecurrentServiceDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<RecurrentServiceDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,RecurrentServiceDto[].class,params);
        return Arrays.asList(response.getBody());
    }

    public RecurrentServiceDto getById(String id){
        final String uri = url + "/v1/recurrent-service-dto/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);

        HttpEntity<RecurrentServiceDto> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<RecurrentServiceDto> response = restTemplate.exchange(uri, HttpMethod.GET,entity,RecurrentServiceDto.class,params);
        return response.getBody();
    }

    public List<RecurrentServiceDto> getByCreatedByAndState(String createdBy){
        final String uri = url + "/v1/recurrent-service-dto/getByCreatedByAndState/{createdby}";
        Map<String,String> params = new HashMap<>();
        params.put("createdby",createdBy);

        HttpEntity<RecurrentServiceDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<RecurrentServiceDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,RecurrentServiceDto[].class,params);
        return Arrays.asList(response.getBody());
    }
}
