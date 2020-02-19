package com.rhine.launch;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

@Slf4j
public class ProcessUtilsTest {

    @Test
    public void process() {

        Map<Integer, String> integerStringMap = ProcessUtils.listProcessByJps();

        for (Map.Entry<Integer, String> e : integerStringMap.entrySet()) {
            log.info("key = {}, value = {}", e.getKey(), e.getValue());
        }


    }
}
