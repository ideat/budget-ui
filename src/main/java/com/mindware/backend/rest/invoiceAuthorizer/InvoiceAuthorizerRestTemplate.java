package com.mindware.backend.rest.invoiceAuthorizer;

import com.mindware.backend.entity.invoiceAuthorizer.InvoiceAuthorizer;
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
public class InvoiceAuthorizerRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public InvoiceAuthorizer add(InvoiceAuthorizer invoiceAuthorizer){
        final String uri = url + "/v1/invoice-authorizer/add";
        HttpEntity<InvoiceAuthorizer> entity = new HttpEntity<>(invoiceAuthorizer, HeaderJwt.getHeader());
        ResponseEntity<InvoiceAuthorizer> response = restTemplate.postForEntity(uri,entity,InvoiceAuthorizer.class);
        return response.getBody();
    }

    public InvoiceAuthorizer getById(String id){
        final String uri = url + "/v1/invoice-authorizer/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id",id);
        HttpEntity<InvoiceAuthorizer> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<InvoiceAuthorizer> response = restTemplate.exchange(uri, HttpMethod.GET,entity,InvoiceAuthorizer.class,params);
        return response.getBody();
    }

    public InvoiceAuthorizer getByCodeEmployee(Integer code){
        final String uri = url + "/v1/invoice-authorizer/getByCodeEmployee/{code}";
        Map<String,Integer> params = new HashMap<>();
        params.put("code",code);
        HttpEntity<InvoiceAuthorizer> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<InvoiceAuthorizer> response = restTemplate.exchange(uri, HttpMethod.GET,entity,InvoiceAuthorizer.class,params);
        return response.getBody();
    }

    public List<InvoiceAuthorizer> getAll(){
        final String uri = url + "/v1/invoice-authorizer/getAll";
        HttpEntity<InvoiceAuthorizer[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<InvoiceAuthorizer[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,InvoiceAuthorizer[].class);
        return Arrays.asList(response.getBody());
    }

    public List<InvoiceAuthorizer> getByCodeBranchOffice(Integer codeBranchOffice){
        final String uri = url + "/v1/invoice-authorizer/getByCodeBranchOffice/{codebranchoffice}";
        Map<String,Integer> params = new HashMap<>();
        params.put("codebranchoffice",codeBranchOffice);

        HttpEntity<InvoiceAuthorizer[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<InvoiceAuthorizer[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,InvoiceAuthorizer[].class,params);
        return Arrays.asList(response.getBody());
    }
}
