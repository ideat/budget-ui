package com.mindware.backend.rest.obligations;

import com.mindware.backend.entity.obligations.ObligationsDto;
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
public class ObligationsDtoRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public List<ObligationsDto> getAll(){
        final String uri = url + "/v1/obligationsdto/getAll";
        HttpEntity<ObligationsDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<ObligationsDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,ObligationsDto[].class);
        return Arrays.asList(response.getBody());
    }

    public List<ObligationsDto> getByPeriod(String period){
        final String uri = url + "/v1/obligationsdto/getByPeriod/{period}";
        Map<String,String> params = new HashMap<>();
        params.put("period",period);
        HttpEntity<ObligationsDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<ObligationsDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,ObligationsDto[].class, params);
        return Arrays.asList(response.getBody());
    }

    public List<ObligationsDto> getByYear(Integer year){
        final String uri = url + "/v1/obligationsdto/getByYear/{period}";
        Map<String,Integer> params = new HashMap<>();
        params.put("year",year);
        HttpEntity<ObligationsDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<ObligationsDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,ObligationsDto[].class, params);
        return Arrays.asList(response.getBody());
    }

    public ObligationsDto getById(String id){
        final String uri = url + "/v1/obligationsdto/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);
        HttpEntity<ObligationsDto> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<ObligationsDto> response = restTemplate.exchange(uri, HttpMethod.GET,entity,ObligationsDto.class, params);
        return response.getBody();
    }
}
