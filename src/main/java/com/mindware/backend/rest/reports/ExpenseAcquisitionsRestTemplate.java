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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ExpenseAcquisitionsRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    @Value("${path_xls_report}")
    private String path;

    public byte[] reportExpenseAcquisition(String period, String format){
        final String uri = url + "/v1/report/executivereport/expenseAcquisition";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("period",period);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();
    }

    public String reportExpenseAcquisitionXls(String period, String format){
        final String uri = url + "/v1/report/executivereport/expenseAcquisition";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("period",period);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        String fullPath = path+"/ea"+ UUID.randomUUID().toString()+".xlsx";

        try {
            Files.write(Paths.get(fullPath),response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;

    }

    public byte[] reportAcquisitionDetail(String period, String format){
        final String uri = url + "/v1/report/acquisitionreport/acquisitionDetailReport";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("period",period);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();
    }

    public String reportAcquisitionDetailXls(String period, String format){
        final String uri = url + "/v1/report/acquisitionreport/acquisitionDetailReport";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("period",period);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        String fullPath = path+"/ea"+ UUID.randomUUID().toString()+".xlsx";

        try {
            Files.write(Paths.get(fullPath),response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;
    }

    public byte[] reportBasicRecurrentService(String period, String format){
        final String uri = url + "/v1/report/basicRecurrentService/basicRecurrentServiceReport";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("period",period);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();
    }

    public String reportBasicRecurrentServiceXls(String period, String format){
        final String uri = url + "/v1/report/basicRecurrentService/basicRecurrentServiceReport";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("period",period);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        String fullPath = path+"/ea"+ UUID.randomUUID().toString()+".xlsx";

        try {
            Files.write(Paths.get(fullPath),response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;
    }
}
