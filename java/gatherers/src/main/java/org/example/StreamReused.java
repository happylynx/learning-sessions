package org.example;

import java.util.List;
import java.util.stream.Stream;

public class StreamReused {
    public static void main(String[] args) {
        Stream<Integer> stream = List.of(1, 2).stream();
        System.out.println(stream.toList());
        System.out.println(stream.toList()); // throws IllegalStateException: stream has already been operated upon or closed
    }
}
