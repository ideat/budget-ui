package com.mindware.backend.rest.reports;

import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExpenseAcquisitionsRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public byte[] report(String period){
        final String uri = url + "/v1/report/executivereport/expenseAcquisition";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("period",period);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();
    }

}
