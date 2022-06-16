package com.mindware.backend.util;

import com.mindware.backend.entity.rol.Option;
import com.mindware.ui.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

public class GrantOptions {


    public static boolean grantedOption(String option){

        List<Option> options =   MainLayout.get().optionList.stream().filter(value -> value.getName().equals(option))
                .collect(Collectors.toList());

        return options.get(0).isWrite();
    }
}
