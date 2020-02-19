package com.rhine.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 测试
 *
 * @author LDZ
 * @date 2019-12-04 20:37
 */
public class Math {
    private static Random random = new Random();

    private int illegalArgumentCount = 0;

    public static void main(String[] args) throws InterruptedException {
        Math game = new Math();
        while (true) {
            game.run();
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private void run() {
        try {
            int number = random.nextInt() / 10000;
            List<Integer> primeFactors = primeFactors(number);
            print(number, primeFactors);

        } catch (Exception e) {
            System.out.println(String.format("illegalArgumentCount:%3d, ", illegalArgumentCount) + e.getMessage());
        }
    }

    private static void print(int number, List<Integer> primeFactors) {
        StringBuffer sb = new StringBuffer(number + "=");
        for (int factor : primeFactors) {
            sb.append(factor).append('*');
        }
        if (sb.charAt(sb.length() - 1) == '*') {
            sb.deleteCharAt(sb.length() - 1);
        }
        System.out.println(sb);
    }

    private List<Integer> primeFactors(int number) {
        if (number < 2) {
            illegalArgumentCount++;
            throw new IllegalArgumentException("number is: " + number + ", need >= 2");
        }

        List<Integer> result = new ArrayList<Integer>();
        int i = 2;
        while (i <= number) {
            if (number % i == 0) {
                result.add(i);
                number = number / i;
                i = 2;
            } else {
                i++;
            }
        }

        return result;
    }
}
