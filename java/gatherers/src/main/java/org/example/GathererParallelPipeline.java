package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.IntStream;

public class GathererParallelPipeline {
    public static void main(String[] args) {
        List<Integer> result = IntStream.range(0, 10)
                .parallel()
                .boxed()
                .peek(item -> System.out.println("Peek1 " + item + " " + Thread.currentThread()))
                .gather(passThrough())
                .peek(item -> System.out.println("Peek2 " + item + " " + Thread.currentThread()))
                .toList();
        System.out.println(result);
    }

    private static Gatherer<Integer, List<Integer>, Integer> passThrough() {
        return Gatherer.of(
                () -> {
                    List<Integer> state = new ArrayList<>() {
                        @Override
                        public String toString() {
                            return super.toString() + System.identityHashCode(this);
                        }
                    };
                    System.out.println("Initializer " + state + " " + Thread.currentThread());
                    return state;
                },
                (state, item, sink) -> {
                    System.out.println("Integrator (" + state + ", " + item + ") " + Thread.currentThread());
                    state.add(item);
                    return sink.push(item);
                },
                (object1, object2) -> {
                    System.out.println("Combiner (" + object1 + ", " + object2 + ") " + Thread.currentThread());
                    object1.addAll(object2);
                    return object1;
                },
                (state, sink) -> {
                    System.out.println("Finisher " + state + " " + Thread.currentThread());
                }
        );
    }

}
