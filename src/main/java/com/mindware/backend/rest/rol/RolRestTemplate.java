package com.mindware.backend.rest.rol;

import com.mindware.backend.entity.rol.Rol;
import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RolRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public Rol add(Rol rol){
        final String uri = url + "/v1/rol/add";
        HttpEntity<Rol> entity = new HttpEntity<>(rol, HeaderJwt.getHeader());
        ResponseEntity<Rol> response = restTemplate.postForEntity(uri,entity,Rol.class);
        return response.getBody();
    }

    public List<Rol> getAllRols(){
        final String uri = url + "/v1/rol/getAll";
        HttpEntity<Rol[]> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<Rol[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Rol[].class);
        Rol[] rols = response.getBody();
        return Arrays.asList(rols);
    }

    public Rol getById(UUID id){
        final String uri = url + "/v1/rol/getById/{id}";
        Map<String,UUID> params = new HashMap<>();
        params.put("id",id);
        HttpEntity<Rol> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<Rol> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Rol.class,params);

        return response.getBody();
    }

    public Rol getRolByName(String name){
        final String uri = url + "/v1/rol/getByName/{name}";
        Map<String,String> params = new HashMap<>();
        params.put("name",name);
        HttpEntity<Rol> entity = new HttpEntity<>(HeaderJwt.getHeader());
        ResponseEntity<Rol> response = restTemplate.exchange(uri, HttpMethod.GET,entity,Rol.class,params);

        return response.getBody();
    }

    public void update(Rol rol){
        final String uri = url + "/v1/rol/update";
        HttpEntity<Rol> entity = new HttpEntity<>(rol,HeaderJwt.getHeader());
        restTemplate.put(uri,entity);
    }

}
