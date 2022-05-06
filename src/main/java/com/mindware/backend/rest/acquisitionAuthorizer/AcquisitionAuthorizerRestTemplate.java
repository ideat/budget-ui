package com.mindware.backend.rest.acquisitionAuthorizer;

import com.mindware.backend.entity.acquisitionAuthorizer.AcquisitionAuthorizer;
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
public class AcquisitionAuthorizerRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public AcquisitionAuthorizer add(AcquisitionAuthorizer acquisitionAuthorizer){
        final String uri = url + "/v1/acquisition-authorizer/add";
        HttpEntity<AcquisitionAuthorizer> entity = new HttpEntity<>(acquisitionAuthorizer, HeaderJwt.getHeader());
        ResponseEntity<AcquisitionAuthorizer> response = restTemplate.postForEntity(uri,entity,AcquisitionAuthorizer.class);
        return response.getBody();
    }

    public AcquisitionAuthorizer  getById(String id){
        final String uri = url + "/v1/acquisition-authorizer/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);
        HttpEntity<AcquisitionAuthorizer> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<AcquisitionAuthorizer> response = restTemplate.exchange(uri, HttpMethod.GET, entity, AcquisitionAuthorizer.class,params);
        return response.getBody();
    }

    public List<AcquisitionAuthorizer> getAll(){
        final String uri = url + "/v1/acquisition-authorizer/getAll";
        HttpEntity<AcquisitionAuthorizer[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<AcquisitionAuthorizer[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity,AcquisitionAuthorizer[].class);
        return Arrays.asList(response.getBody());
    }

    public List<AcquisitionAuthorizer> getByCodeBranchOffice(Integer codeBranchOffice){
        final String uri = url + "/v1/acquisition-authorizer/getByCodeBranchOffice/{codebranchoffice}";
        Map<String,Integer> params = new HashMap<>();
        params.put("codebranchoffice",codeBranchOffice);

        HttpEntity<AcquisitionAuthorizer[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<AcquisitionAuthorizer[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity,AcquisitionAuthorizer[].class,params);
        return Arrays.asList(response.getBody());
    }
}
