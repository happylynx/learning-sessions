package org.example;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FlatMapMapMulti {
    public static void main(String[] args) {
        List<Integer> result = Stream.<List<Integer>>of(
                        List.of(1, 2),
                        List.of(),
                        List.of(3)
                )
//                .<Integer>mapMulti(Iterable::forEach)
//                .mapMulti((integerList, sink) -> integerList.forEach(sink))
                .mapMulti((List<Integer> integerList, Consumer<Integer> sink) -> {
                    for (Integer i : integerList) {
                        sink.accept(i);
                    }
                })
                .toList();
        System.out.println(result); // [1, 2, 3]
    }
}
