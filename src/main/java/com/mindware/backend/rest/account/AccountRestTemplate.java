package com.mindware.backend.rest.account;

import com.mindware.backend.entity.config.Account;
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
public class AccountRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public Account add(Account account){
        final String uri = url + "/v1/account/add";
        HttpEntity<Account> entity = new HttpEntity<>(account, HeaderJwt.getHeader());
        ResponseEntity<Account> response = restTemplate.postForEntity(uri,entity,Account.class);
        return response.getBody();
    }

    public List<Account> getAll(){
        final String uri = url + "/v1/account/getAll";
        HttpHeaders headers =  HeaderJwt.getHeader();
        HttpEntity<Account[]> entity = new HttpEntity<>(headers);
        ResponseEntity<Account[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Account[].class);
        return Arrays.asList(response.getBody());
    }

    public List<Account> getAllByPeriod(Integer period){
        final String uri = url + "/v1/account/getAllByPeriod/{period}";
        Map<String,Integer> params = new HashMap<>();
        params.put("period", period);

        HttpHeaders headers = HeaderJwt.getHeader();
        HttpEntity<Account[]> entity = new HttpEntity<>(headers);
        ResponseEntity<Account[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Account[].class,params);
        return Arrays.asList(response.getBody());
    }

    public Account getById(String id){
        final String uri = url + "/v1/account/getById/{id}";
        Map<String,String> params = new HashMap<>();
        params.put("id", id);

        HttpHeaders headers =  HeaderJwt.getHeader();
        HttpEntity<Account> entity = new HttpEntity<>(headers);
        ResponseEntity<Account> response = restTemplate.exchange(uri,HttpMethod.GET,entity,Account.class,params);
        return response.getBody();
    }

    public List<Account> getByCodeBusinessAndPeriod(Integer codeBusinessUnit, Integer period){
        final String uri = url + "/v1/account/getByCodeBusinessAndPeriod/{codebusinessunit}/{period}";
        Map<String,Integer> params = new HashMap<>();
        params.put("period", period);
        params.put("codebusinessunit",codeBusinessUnit);

        HttpHeaders headers = HeaderJwt.getHeader();
        HttpEntity<Account[]> entity = new HttpEntity<>(headers);
        ResponseEntity<Account[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Account[].class,params);
        return Arrays.asList(response.getBody());
    }

    public List<Account> cloneAccount(Integer origin, Integer posting){
        final String uri = url + "/v1/account/cloneAccount/{origin}/{destination}";
        Map<String,Integer> params = new HashMap<>();
        params.put("origin",origin);
        params.put("destination",posting);

        HttpHeaders headers =  HeaderJwt.getHeader();
        HttpEntity<Account[]> entity = new HttpEntity<>(headers);
        ResponseEntity<Account[]> response = restTemplate.exchange(uri,HttpMethod.POST,entity,Account[].class,params);
        return Arrays.asList(response.getBody());
    }

}
