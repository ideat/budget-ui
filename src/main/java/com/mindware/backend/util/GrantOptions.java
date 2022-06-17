package com.mindware.backend.util;

import com.mindware.backend.entity.rol.Option;
import com.mindware.ui.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

public class GrantOptions {


    public static boolean grantedOptionWrite(String option){

        List<Option> options =   MainLayout.get().optionList.stream().filter(value -> value.getName().equals(option))
                .collect(Collectors.toList());

        return options.get(0).isWrite();
    }
    public static boolean grantedOptionRead(String option){

        List<Option> options =   MainLayout.get().optionList.stream().filter(value -> value.getName().equals(option))
                .collect(Collectors.toList());

        return options.get(0).isRead();
    }

    public static boolean grantedOptionSend(String option){

        List<Option> options =   MainLayout.get().optionList.stream().filter(value -> value.getName().equals(option))
                .collect(Collectors.toList());

        return options.get(0).isSend();
    }

    public static boolean grantedOptionObserved(String option){

        List<Option> options =   MainLayout.get().optionList.stream().filter(value -> value.getName().equals(option))
                .collect(Collectors.toList());

        return options.get(0).isObserved();
    }

    public static boolean grantedOptionFinish(String option){

        List<Option> options =   MainLayout.get().optionList.stream().filter(value -> value.getName().equals(option))
                .collect(Collectors.toList());

        return options.get(0).isFinish();
    }

    public static boolean grantedOptionAccounting(String option){

        List<Option> options =   MainLayout.get().optionList.stream().filter(value -> value.getName().equals(option))
                .collect(Collectors.toList());

        return options.get(0).isAccounting();
    }

}
