package com.mindware.backend.util;

import com.mindware.backend.entity.config.Parameter;
import com.mindware.backend.rest.parameter.ParameterRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UtilValues {

    @Autowired
    ParameterRestTemplate parameterRestTemplate;

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
}
