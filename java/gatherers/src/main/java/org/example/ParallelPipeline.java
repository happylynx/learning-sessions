package org.example;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParallelPipeline {
    public static void main(String[] args) {
        String result = Stream.of(1, 2, 3, null, 5, 7, 6, 4)
                .parallel()
                .filter(ParallelPipeline::removeNull)
                .sorted(ParallelPipeline::ascComparator)
                .filter(ParallelPipeline::remove4)
                .map(ParallelPipeline::intToString)
                .collect(Collectors.joining(", "));
        System.out.println(result);
    }

    private static boolean removeNull(Integer integer) {
        System.out.println("removeNull() " + integer + " " + Thread.currentThread());
        return integer != null;
    }

    private static boolean remove4(Integer integer) {
        System.out.println("remove4() " + integer + " " + Thread.currentThread());
        return integer != 4;
    }

    private static int ascComparator(Integer i1, Integer i2) {
        System.out.println("ascComparator() (" + i1 + ", " + i2 + ") " + Thread.currentThread());
        return i1.compareTo(i2);
    }

    private static String intToString(Integer i) {
        System.out.println("intToString() " + i + " " + Thread.currentThread());
        return "" + i;
    }
}
