package org.example;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.IntStream;

public class AverageGatherer {
    public static void main(String[] args) {
        final Average average = new Average();
        List<Integer> list = IntStream.range(0, 5)
                .boxed()
                .parallel()
                .gather(average)
                .toList();
        System.out.println(list);
        System.out.println("average = " + average.getResult());
    }

    private static class Average implements Gatherer<Integer, Average.Accumulator, Integer> {

        private float result;

        @Override
        public Supplier<Accumulator> initializer() {
            return Accumulator::new;
        }

        @Override
        public Integrator<Accumulator, Integer, Integer> integrator() {
            return (accumulator, item, sink) -> {
                accumulator.sum += item;
                accumulator.count += 1;
                return sink.push(item);
            };
        }

        @Override
        public BinaryOperator<Accumulator> combiner() {
            return (accumulator, accumulator2) -> {
                accumulator.sum += accumulator2.sum;
                accumulator.count += accumulator2.count;
                return accumulator;
            };
        }

        @Override
        public BiConsumer<Accumulator, Downstream<? super Integer>> finisher() {
            return (accumulator, downstream) -> {
                result = (float) accumulator.sum / accumulator.count;
            };
        }

        public float getResult() {
            return result;
        }

        private static class Accumulator {
            int sum = 0;
            int count = 0;
        }
    }
}
