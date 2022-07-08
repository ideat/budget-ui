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
public class ExpenseServicesRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    @Value("${path_xls_report}")
    private String path;

    public byte[] reportExpensesServiceDetail( String codeFatherBusinessUnit, String cutOffDate,
                                               String nameBusinessUnit, String typeReport, String format){
        final String uri = url + "/v1/report/expensesService/expenseServiceDetailBusinessUnit";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("codefatherbusinessunit",codeFatherBusinessUnit);
        headers.set("cutoffdate", cutOffDate);
        headers.set("namebusinessunit",nameBusinessUnit);
        headers.set("typeReport",typeReport);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();

    }

    public String reportExpensesServiceDetailXls( String codeFatherBusinessUnit, String cutOffDate,
                                               String nameBusinessUnit, String typeReport, String format){
        final String uri = url + "/v1/report/expensesService/expenseServiceDetailBusinessUnit";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("codefatherbusinessunit",codeFatherBusinessUnit);
        headers.set("cutoffdate", cutOffDate);
        headers.set("namebusinessunit",nameBusinessUnit);
        headers.set("typeReport",typeReport);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        String fullPath = path+"/es"+ UUID.randomUUID().toString()+".xlsx";

        try {
            Files.write(Paths.get(fullPath),response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;

    }

    public byte[] reportExpensesServiceBusinessUnit( String codeFatherBusinessUnit, String cutOffDate, String nameBusinessUnit, String format){
        final String uri = url + "/v1/report/expensesService/expenseServiceBusinessUnit";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("codefatherbusinessunit",codeFatherBusinessUnit);
        headers.set("cutoffdate", cutOffDate);
        headers.set("namebusinessunit",nameBusinessUnit);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();

    }

    public String reportExpensesServiceBusinessUnitXls( String codeFatherBusinessUnit, String cutOffDate, String nameBusinessUnit, String format){
        final String uri = url + "/v1/report/expensesService/expenseServiceBusinessUnit";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("codefatherbusinessunit",codeFatherBusinessUnit);
        headers.set("cutoffdate", cutOffDate);
        headers.set("namebusinessunit",nameBusinessUnit);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        String fullPath = path+"/es"+ UUID.randomUUID().toString()+".xlsx";

        try {
            Files.write(Paths.get(fullPath),response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;

    }

    public byte[] reportExpensesServiceResume( String codeFatherBusinessUnit, String cutOffDate, String nameBusinessUnit, String format){
        final String uri = url + "/v1/report/expensesService/expenseServiceResumeBusinessUnit";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("codefatherbusinessunit",codeFatherBusinessUnit);
        headers.set("cutoffdate", cutOffDate);
        headers.set("namebusinessunit",nameBusinessUnit);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();

    }

    public String reportExpensesServiceResumeXls( String codeFatherBusinessUnit, String cutOffDate, String nameBusinessUnit, String format){
        final String uri = url + "/v1/report/expensesService/expenseServiceResumeBusinessUnit";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("codefatherbusinessunit",codeFatherBusinessUnit);
        headers.set("cutoffdate", cutOffDate);
        headers.set("namebusinessunit",nameBusinessUnit);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        String fullPath = path+"/es"+ UUID.randomUUID().toString()+".xlsx";

        try {
            Files.write(Paths.get(fullPath),response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;

    }

    public byte[] reportExpenseConsolidated(String cutOffDate, String format){
        final String uri = url + "/v1/report/expensesService/expenseConsolidated";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("cutoffdate", cutOffDate);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();

    }

    public String reportExpenseConsolidatedXls(String cutOffDate, String format){
        final String uri = url + "/v1/report/expensesService/expenseConsolidated";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("cutoffdate", cutOffDate);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        String fullPath = path+"/es"+ UUID.randomUUID().toString()+".xlsx";

        try {
            Files.write(Paths.get(fullPath),response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullPath;

    }
}
