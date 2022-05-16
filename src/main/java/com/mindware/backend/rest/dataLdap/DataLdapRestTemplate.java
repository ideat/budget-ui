package com.mindware.backend.rest.dataLdap;

import com.mindware.backend.entity.user.UserLdapDto;
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
public class DataLdapRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public List<UserLdapDto> getByCodeBusinessUnit(Integer division){
        final String uri = url + "/ldap/getUserByDivision/{division}";
        Map<String,Integer> params = new HashMap<>();
        params.put("division", division);
        HttpEntity<UserLdapDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<UserLdapDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,UserLdapDto[].class,params);
        return Arrays.asList(response.getBody());
    }

    public List<UserLdapDto> getUserByCriteria(String criteria, String value){
        final String uri = url + "/ldap/getUserByCriteria/{criteria}/{value}";
        Map<String,String> params = new HashMap<>();
        params.put("criteria",criteria);
        params.put("value", value);
        HttpEntity<UserLdapDto[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<UserLdapDto[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,UserLdapDto[].class,params);
        return Arrays.asList(response.getBody());
    }
}
