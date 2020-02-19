package com.rhine.core.config;

import com.rhine.util.annotation.Description;
import com.rhine.util.annotation.Option;
import lombok.Data;

/**
 * @author LDZ
 * @date 2020-02-19 15:43
 */
@Data
public class Configure {

    /**
     * java process pid
     */
    @Option(shortName = "p", longName = "pid", required = true)
    @Description("java process pid")
    public Integer javaPid;


}
