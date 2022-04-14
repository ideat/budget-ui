package com.mindware.backend.rest.contract;

import com.mindware.backend.entity.contract.ContractDto;
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
public class ContractDtoRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public List<ContractDto> getAll(){
        final String uri = url + "/v1/contractDto/getAll";
        HttpEntity<ContractDto[]> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<ContractDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,ContractDto[].class);
        return Arrays.asList(response.getBody());
    }

    public ContractDto getByIdContract(String idContract){
        final String uri = url + "/v1/contractDto/getByIdContract/{idcontract}";
        Map<String,String> params = new HashMap<>();
        params.put("idcontract", idContract);

        HttpEntity<ContractDto> entity = new HttpEntity<>( HeaderJwt.getHeader());
        ResponseEntity<ContractDto> response = restTemplate.exchange(uri, HttpMethod.GET,entity,ContractDto.class,params);
        return response.getBody();
    }
}
