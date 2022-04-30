package com.mindware.backend.rest.acquisition;

import com.mindware.backend.entity.adquisition.AcquisitionDto;
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
public class AcquisitionDtoRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public List<AcquisitionDto> getAll(){
        final String uri = url + "/v1/acquisitionDto/getAll";
        HttpEntity<AcquisitionDto> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<AcquisitionDto[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, AcquisitionDto[].class);
        return Arrays.asList(response.getBody());
    }

    public List<AcquisitionDto> getByCodeBusinessUnit(Integer codebusinessunit){
        final String uri = url + "/v1/acquisitionDto/getByCodeBusinessUnit/{codebusinessunit}";
        Map<String,Integer> params = new HashMap<>();
        params.put("codebusinessunit",codebusinessunit);
        HttpEntity<AcquisitionDto> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<AcquisitionDto[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, AcquisitionDto[].class, params);
        return Arrays.asList(response.getBody());
    }
}
