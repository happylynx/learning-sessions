package org.example;

import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class ParallelPipelineWithCustomCollector {
    public static void main(String[] args) {
        String result = Stream.of(1, 2, 3, null, 5, 7, 6, 4)
                .parallel()
                .filter(ParallelPipelineWithCustomCollector::removeNull)
                .sorted(ParallelPipelineWithCustomCollector::ascComparator)
                .filter(ParallelPipelineWithCustomCollector::remove4)
                .map(ParallelPipelineWithCustomCollector::intToString)
                .collect(join());
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

    private static Collector<String, ?, String> join() {
        return Collector.of(
                () -> {
                    System.out.println("join() supplier " + Thread.currentThread());
                    return new ArrayList<String>();
                },
                (list, item) -> {
                    System.out.println("join() accumulator (" + list + ", " + item + ") " + Thread.currentThread());
                    list.add(item);
                },
                (list1, list2) -> {
                    System.out.println("join() combiner (" + list1 + ", " + list2 + ") " + Thread.currentThread());
                    list1.addAll(list2);
                    return list1;
                },
                list -> {
                    System.out.println("join() finisher " + list + " " + Thread.currentThread());
                    return String.join(", ", list);
                }
        );
    }
}
