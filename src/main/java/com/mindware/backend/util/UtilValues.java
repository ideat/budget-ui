package com.mindware.backend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.config.Account;
import com.mindware.backend.entity.config.Parameter;
import com.mindware.backend.entity.config.Period;
import com.mindware.backend.entity.config.SubAccount;
import com.mindware.backend.rest.account.AccountRestTemplate;
import com.mindware.backend.rest.parameter.ParameterRestTemplate;
import com.mindware.backend.rest.period.PeriodRestTemplate;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UtilValues {

    @Autowired
    ParameterRestTemplate parameterRestTemplate;

    @Autowired
    PeriodRestTemplate periodRestTemplate;

    @Autowired
    AccountRestTemplate accountRestTemplate;

    private String[] months = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO"
            ,"JULIO","AGOSTO","SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE","DICIEMBRE"};

    public List<String> getValueParameterByCategory(String category){
        List<Parameter> parameterList = parameterRestTemplate.getByCategory(category);
        return parameterList.stream()
                .map(Parameter::getValue)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Integer> getValueIntParameterByCategory(String category){
        List<Parameter> parameterList = parameterRestTemplate.getByCategory(category);
        return parameterList.stream()
                .map(Parameter::getValue)
                .map(Integer::valueOf)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Integer> getPeriods(){
        List<Integer> periods = new ArrayList<>();
        List<Period> periodList = periodRestTemplate.getAll();
        periods = periodList.stream()
                .map(Period::getYear)
                .collect(Collectors.toList());
        return periods;

    }

    public Integer getActivePeriod(){
        List<Period> periodList = periodRestTemplate.getAll();
        return periodList.stream()
                .filter(p ->p.getIsOpen().equals(true))
                .map(Period::getYear)
                .collect(Collectors.toList()).get(0);
    }

    public List<String> getAccounts(){
        List<Account> accounts = accountRestTemplate.getAllByPeriod(getActivePeriod());
        List<String> accountNumberList = accounts.stream()
                .map(Account::getNumberAccount)
                .collect(Collectors.toList());
        return accountNumberList;
    }

    @SneakyThrows
    public List<String> getSubAccounts(String numberAccount)  {
        List<Account> accounts = accountRestTemplate.getAllByPeriod(getActivePeriod());
        Account account = accounts.stream()
                .filter(p -> p.getNumberAccount().equals(numberAccount))
                .findFirst().get();
        ObjectMapper mapper = new ObjectMapper();
        List<SubAccount> subAccounts = mapper.readValue(account.getSubAccount(), new TypeReference<List<SubAccount>>() {});
        List<String> subAccountNumberList = subAccounts.stream()
                .map(SubAccount::getNumberSubAccount)
                .collect(Collectors.toList());
        return subAccountNumberList;
    }

    public List<String> generatePeriods(){
        String year = String.valueOf(getActivePeriod());
        List<String> periods = new ArrayList<>();

        for(String s: months){
            String period = s.concat("/").concat(year);
            periods.add(period);
        }

        return periods;
    }
}
