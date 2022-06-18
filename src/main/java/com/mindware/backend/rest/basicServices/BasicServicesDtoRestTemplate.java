package com.mindware.backend.rest.basicServices;

import com.mindware.backend.entity.basicServices.BasicServicesDto;
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
public class BasicServicesDtoRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public List<BasicServicesDto> getAll(){
        final String uri = url + "/v1/basicservicesdto/getAll";
        HttpEntity<BasicServicesDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<BasicServicesDto[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, BasicServicesDto[].class);
        return Arrays.asList(response.getBody());
    }

    public List<BasicServicesDto> getAllByPeriod(String period){
        final String uri = url + "/v1/basicservicesdto/getAllByPeriod/{period}";
        Map<String,String> params = new HashMap<>();
        params.put("period",period);

        HttpEntity<BasicServicesDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<BasicServicesDto[]> response = restTemplate.exchange(uri,HttpMethod.GET, entity,BasicServicesDto[].class,params);
        return Arrays.asList(response.getBody());
    }

    public BasicServicesDto getByIdBasicServices(String id){
        final String uri = url + "/v1/basicservicesdto/getByIdBasicServices/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);

        HttpEntity<BasicServicesDto> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<BasicServicesDto> response = restTemplate.exchange(uri,HttpMethod.GET,entity,BasicServicesDto.class,params);
        return response.getBody();
    }

    public List<BasicServicesDto> getByCreatedByAndState(String createBy){
        final String uri = url + "/v1/basicservicesdto/getByCreatedByAndState/{createdby}";
        Map<String,String> params = new HashMap<>();
        params.put("createdby",createBy);

        HttpEntity<BasicServicesDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<BasicServicesDto[]> response = restTemplate.exchange(uri,HttpMethod.GET, entity,BasicServicesDto[].class,params);
        return Arrays.asList(response.getBody());
    }
}
