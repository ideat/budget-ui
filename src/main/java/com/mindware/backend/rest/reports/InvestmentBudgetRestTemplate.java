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
public class InvestmentBudgetRestTemplate {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url_budget}")
    private String url;

    @Value("${path_xls_report}")
    private String path;

    public byte[] reportInvestmentBudgetDetail(String year, String codeFatherBusinessUnit,
                                               String cutOffDate, String nameBusinessUnit, String format){
        final String uri = url + "/v1/report/investementbudgetreport/investmentBudgetDetail";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("year",year);
        headers.set("codefatherbusinessunit",codeFatherBusinessUnit);
        headers.set("cutoffdate", cutOffDate);
        headers.set("namebusinessunit",nameBusinessUnit);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();
    }

    public String reportInvestmentBudgetDetailXls(String year, String codeFatherBusinessUnit,
                                               String cutOffDate, String nameBusinessUnit, String format){
        final String uri = url + "/v1/report/investementbudgetreport/investmentBudgetDetail";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("year",year);
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

    public byte[] reportInvestmentBudgetGroupedBusinessUnit(String cutOffDate, String format){
        final String uri = url + "/v1/report/investementbudgetreport/investmentBudgetGroupedBydBusinessUnit";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("cutoffdate", cutOffDate);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();
    }

    public String reportInvestmentBudgetGroupedBusinessUnitXls(String cutOffDate, String format){
        final String uri = url + "/v1/report/investementbudgetreport/investmentBudgetGroupedBydBusinessUnit";
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

    public byte[] reportInvestmentBudgetExecutive(String cutOffDate, String format){
        final String uri = url + "/v1/report/investementbudgetreport/investmentBudgetExecutive";
        HttpHeaders headers = HeaderJwt.getHeader();
        headers.set("cutoffdate", cutOffDate);
        headers.set("format",format);

        HttpEntity<byte[]> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> reponse = restTemplate.exchange(uri, HttpMethod.GET,entity,byte[].class);
        return reponse.getBody();
    }

    public String reportInvestmentBudgetExecutiveXls(String cutOffDate, String format){
        final String uri = url + "/v1/report/investementbudgetreport/investmentBudgetExecutive";
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
