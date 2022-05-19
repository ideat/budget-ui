package com.mindware.backend.rest.typeChangeCurrency;

import com.mindware.backend.entity.config.TypeChangeCurrency;
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
public class TypeChangeCurrencyRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public TypeChangeCurrency add(TypeChangeCurrency typeChangeCurrency){
        final String uri = url + "/v1/typechangecurrency/add";
        HttpEntity<TypeChangeCurrency> entity = new HttpEntity<>(typeChangeCurrency, HeaderJwt.getHeader());
        ResponseEntity<TypeChangeCurrency> response = restTemplate.postForEntity(uri,entity,TypeChangeCurrency.class);
        return response.getBody();
    }

    public List<TypeChangeCurrency> getAll(){
        final String uri = url + "/v1/typechangecurrency/getAll";
        HttpEntity<TypeChangeCurrency[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<TypeChangeCurrency[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,TypeChangeCurrency[].class);
        return Arrays.asList(response.getBody());
    }

    public TypeChangeCurrency getCurrentTypeChangeCurrency(String name){
        final String uri = url + "/v1/typechangecurrency/getCurrentTypeChangeCurrency/{name}";
        Map<String,String> params = new HashMap<>();
        params.put("name",name);

        HttpEntity<TypeChangeCurrency> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<TypeChangeCurrency> response = restTemplate.exchange(uri, HttpMethod.GET,entity,TypeChangeCurrency.class,params);
        return response.getBody();
    }

    public TypeChangeCurrency getCurrentTypeChangeCurrencyByValidityStart(String name, String validityStarat, String currency){
        final String uri = url + "/v1/typechangecurrency/getCurrentTypeChangeCurrency/{name}";
        Map<String,String> params = new HashMap<>();
        params.put("name",name);
        params.put("validitystart",validityStarat);
        params.put("currency",currency);


        HttpEntity<TypeChangeCurrency> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<TypeChangeCurrency> response = restTemplate.exchange(uri, HttpMethod.GET,entity,TypeChangeCurrency.class,params);
        return response.getBody();
    }
}
