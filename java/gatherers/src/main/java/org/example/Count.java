package org.example;

import java.util.stream.Gatherer;
import java.util.stream.IntStream;

public class Count {
    public static void main(String[] args) {
        IntStream.rangeClosed(1, 10)
                .parallel()
                .boxed()
                .gather(count())
                .forEach(System.out::println);
    }

    private static Gatherer<? super Integer, ?, Integer> count() {
        class State { int sum; }
        return Gatherer.of(
                State::new,
                Gatherer.Integrator.ofGreedy((state, _, _) -> {
                    state.sum++;
                    return true;
                }),
                (stateA, stateB) -> {
                    stateA.sum += stateB.sum;
                    return stateA;
                },
                (state, downstream) -> { downstream.push(state.sum); }
        );
    }
}
