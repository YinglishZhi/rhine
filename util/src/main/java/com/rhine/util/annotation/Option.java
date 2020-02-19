package com.rhine.util.annotation;

/**
 * Option
 *
 * @author LDZ
 * @date 2020-02-19 16:42
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {


    String longName() default "\u0000";

    String shortName() default "\u0000";

    boolean required() default false;

    boolean acceptValue() default true;

}
