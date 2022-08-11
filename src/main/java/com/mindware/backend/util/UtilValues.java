package com.mindware.backend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.config.*;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.entity.user.UserLdapDto;
import com.mindware.backend.rest.account.AccountRestTemplate;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.rest.dataLdap.DataLdapRestTemplate;
import com.mindware.backend.rest.parameter.ParameterRestTemplate;
import com.mindware.backend.rest.period.PeriodRestTemplate;
import com.mindware.backend.rest.typeChangeCurrency.TypeChangeCurrencyRestTemplate;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UtilValues {

    @Autowired
    ParameterRestTemplate parameterRestTemplate;

    @Autowired
    PeriodRestTemplate periodRestTemplate;

    @Autowired
    AccountRestTemplate accountRestTemplate;

    @Autowired
    private DataLdapRestTemplate dataLdapRestTemplate;

    @Autowired
    private ConceptRestTemplate conceptRestTemplate;

    @Autowired
    private TypeChangeCurrencyRestTemplate typeChangeCurrencyRestTemplate;

    private final String[] months = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO"
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

        List<Period> periodList = periodRestTemplate.getAll();
        List<Integer> periods = periodList.stream()
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

    public List<String> getNameAccounts(){
        List<Account> accounts = accountRestTemplate.getAllByPeriod(getActivePeriod());
        List<String> accountNumberList = accounts.stream()
//                .filter(f -> f.getTypeAccount().equals("GASTO"))
                .map(Account::getNameAccount)
                .map(String::trim)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        return accountNumberList;
    }

    public List<String> getNameAccountsByCodeUnit(Integer codeBusiness){
        List<Account> accounts = accountRestTemplate.getByCodeBusinessAndPeriod(codeBusiness,getActivePeriod());
        List<String> accountNumberList = accounts.stream()
                .map(Account::getNameAccount)
                .distinct()
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

    @SneakyThrows
    public List<String> getNameSubAccounts(String nameAccount)  {
        List<Account> accounts = accountRestTemplate.getAllByPeriod(getActivePeriod());
        Optional<Account> account = accounts.stream()
                .filter(p -> p.getNameAccount().equals(nameAccount))
                .findFirst();
        List<SubAccount> subAccounts=new ArrayList<>();
        List<String> subAccountNameList = new ArrayList<>();
        if(account.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
            subAccounts = mapper.readValue(account.get().getSubAccount(), new TypeReference<List<SubAccount>>() {
            });
            subAccountNameList = subAccounts.stream()
                    .map(SubAccount::getNameSubAccount)
                    .collect(Collectors.toList());
        }
        return subAccountNameList;
    }

    @SneakyThrows
    public List<String> getNameSubAccountsByCodeBusinessUnit(String nameAccount, Integer codeBusinessUnit)  {
        List<Account> accounts = accountRestTemplate.getAllByPeriod(getActivePeriod());
        Optional<Account> account = accounts.stream()
                .filter(p -> p.getNameAccount().equals(nameAccount) && p.getCodeBusinessUnit().equals(codeBusinessUnit))
                .findFirst();
        List<SubAccount> subAccounts=new ArrayList<>();
        List<String> subAccountNameList = new ArrayList<>();
        if(account.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
            subAccounts = mapper.readValue(account.get().getSubAccount(), new TypeReference<List<SubAccount>>() {
            });
            subAccountNameList = subAccounts.stream()
                    .map(SubAccount::getNameSubAccount)
                    .collect(Collectors.toList());
        }
        return subAccountNameList;
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

    public List<String> getAllPeriods(){
        List<Period> periodList = periodRestTemplate.getAll();
        List<Integer> yearPeriods = periodList.stream()
                .filter(p ->p.getIsOpen().equals(true))
                .map(Period::getYear)
                .collect(Collectors.toList());

        List<String> periods = new ArrayList<>();

        for(Integer year:yearPeriods) {
            for (String s : months) {
                String period = s.concat("/").concat(year.toString());
                periods.add(period);
            }
        }
        return periods;
    }

    public List<Integer> getAllYears(){
        List<Period> periodList = periodRestTemplate.getAll();
        List<Integer> yearPeriods = periodList.stream()
                .filter(p ->p.getIsOpen().equals(true))
                .map(Period::getYear)
                .collect(Collectors.toList());

        return yearPeriods;
    }

    public List<String> getAllYearsString(){
        List<Period> periodList = periodRestTemplate.getAll();
        List<String> yearPeriods = periodList.stream()
                .filter(p ->p.getIsOpen().equals(true))
                .map(Period::getYear)
                .map(String::valueOf)
                .collect(Collectors.toList());

        return yearPeriods;
    }

    public List<String> getNameUserLdapByCriteria(String criteria, String value){
        List<UserLdapDto> userLdapDtoList = dataLdapRestTemplate.getUserByCriteria(criteria,value);
        List<String> nameUsers = userLdapDtoList.stream()
                .map(UserLdapDto::getCn)
                .collect(Collectors.toList());
        return nameUsers;
    }

    public BigDecimal sumListDouble(List<Double> list){
        return null;
    }

    public List<String> getNameBusinessUnit(){
        List<Concept> conceptList = conceptRestTemplate.getAgencia();
        return conceptList.stream()
                .map(Concept::getDescription)
                .collect(Collectors.toList());
    }

    public TypeChangeCurrency getCurrentTypeChangeCurrency(String name, String currency){
        return typeChangeCurrencyRestTemplate.getCurrentTypeChangeCurrency(name,currency);

    }

    public TypeChangeCurrency getCurrentTypeChangeCurrencyByValidityStart(String name, String validityStart, String currency){
        return typeChangeCurrencyRestTemplate.getCurrentTypeChangeCurrencyByValidityStart(name,validityStart,currency);
    }

    public List<TypeChangeCurrency> getAllTypeChangeCurrency(){
        return typeChangeCurrencyRestTemplate.getAll();
    }

    public static class StringTrimValue implements Converter<String, String> {

        private static final long serialVersionUID = 1L;

        @Override
        public Result<String> convertToModel(String presentation, ValueContext valueContext) {
            return Result.ok(presentation.trim());
        }

        @Override
        public String convertToPresentation(String model, ValueContext valueContext) {
            return model == null?"":model.trim();
        }

    }

}
