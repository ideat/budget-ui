package com.mindware.backend.rest.historyEndedOperations;

import com.mindware.backend.entity.historyEndedOperations.HistoryEndedOperations;
import com.mindware.backend.util.HeaderJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HistoryEndedOperationsRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    public HistoryEndedOperations add(HistoryEndedOperations historyEndedOperations){
        final String uri = url + "/v1/history-ended-operations/add";
        HttpEntity<HistoryEndedOperations> entity = new HttpEntity<>(historyEndedOperations, HeaderJwt.getHeader());
        ResponseEntity<HistoryEndedOperations> response = restTemplate.postForEntity(uri,entity,HistoryEndedOperations.class);
        return response.getBody();
    }
}
