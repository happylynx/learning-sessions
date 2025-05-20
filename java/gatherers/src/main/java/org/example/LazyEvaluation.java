package org.example;

import java.util.stream.IntStream;

/// It shows
/// * infinite streams
/// * lazy evaluation
/// * propagation of demand
/// * short-circuiting operation
public class LazyEvaluation {
    public static void main(String[] args) {
        IntStream.iterate(0, i -> i + 1)
                .peek(i -> System.out.println("after iterate() " + i))
                .flatMap(i -> IntStream.range(0, i).map(_ -> i))
                .peek(i -> System.out.println("after flatMap() " + i))
                .limit(10)
                .forEach(i -> {
                    sleep();
                    System.out.println(i);
                });
    }

    private static void sleep() {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
