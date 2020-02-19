package com.rhine.core;

import lombok.extern.slf4j.Slf4j;

/**
 * @author LDZ
 * @date 2019-11-01 17:22
 */
@Slf4j
public class TestMainJar {

    public static void main(String[] args) {
        System.out.println(new TransClass().getNumber());
        int count = 0;
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            int number = new TransClass().getNumber();
            System.out.println(number);
            if (3 == number || count >= 1000) {
                break;
            }
        }
    }


}
