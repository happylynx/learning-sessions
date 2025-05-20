package org.example;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Parallel_LastSettingMatters {
    public static void main(String[] args) {
        String result = IntStream.range(0, 10)
                .boxed()
                .parallel()
                .sequential()
                .parallel()
                .map(Parallel_LastSettingMatters::mapToString)
                .collect(Collectors.joining(", "));
        System.out.println(result);

        System.out.println("_____");

        String result2 = IntStream.range(0, 10)
                .boxed()
                .sequential()
                .parallel()
                .sequential()
                .map(Parallel_LastSettingMatters::mapToString)
                .collect(Collectors.joining(", "));
        System.out.println(result2);
    }

    private static String mapToString(Integer i) {
        System.out.println(i + " processed by thread " + Thread.currentThread());
        return String.valueOf(i);
    }
}
