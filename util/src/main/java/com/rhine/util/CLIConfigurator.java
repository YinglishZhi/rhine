package com.rhine.util;

import com.rhine.util.annotation.Description;
import com.rhine.util.annotation.Option;
import org.apache.commons.cli.*;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author LDZ
 * @date 2020-02-19 16:30
 */
public class CLIConfigurator {

    public static Options define(Class<?> clazz) {

        Options options = new Options();

        Field[] fs = clazz.getDeclaredFields();
        // 设置私有属性的访问权限
        for (Field f : fs) {
            //f为单个属性
            Option option = f.getAnnotation(Option.class);
            if (null != option) {
                Description description = f.getAnnotation(Description.class);
                org.apache.commons.cli.Option pid = org.apache.commons.cli.Option.builder(option.shortName())
                        .longOpt(option.longName())
                        .hasArg(option.acceptValue())
                        .desc(Optional.ofNullable(description).map(Description::value).orElse(""))
                        .build();
                options.addOption(pid);
            }
        }
        return options;
    }


    public static void inject(CommandLine commandLine, Object o) throws Exception {

        Field[] fs = o.getClass().getDeclaredFields();

        for (Field f : fs) {
            Option option = f.getAnnotation(Option.class);
            if (null != option) {
                Object value = getParsedOptionValue(commandLine, option.longName(), f.getType());
                f.set(o, value);
            }
        }

    }


    private static Object getParsedOptionValue(CommandLine commandLine, String opt, Class clazz) throws ParseException {
        Object result = null;
        String res = commandLine.getOptionValue(opt);
        if (res != null) {

            if (clazz.equals(Integer.class)) {
                result = Integer.valueOf(res);
            } else {
                result = TypeHandler.createValue(res, clazz);
            }
        }
        return result;
    }


}
