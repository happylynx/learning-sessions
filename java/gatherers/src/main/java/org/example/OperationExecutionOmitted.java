package org.example;

import java.util.stream.IntStream;

public class OperationExecutionOmitted {
    public static void main(String[] args) {
        long count = IntStream.range(0, 10)
                .boxed()
//                .filter(OperationExecutionOmitted::remove4)
                .map(OperationExecutionOmitted::intToString)
                .count();
        System.out.println(count);
    }

    private static boolean remove4(Integer integer) {
        System.out.println("remove4() " + integer);
        return integer != 4;
    }

    private static String intToString(Integer integer) {
        System.out.println("intToString() " + integer);
        return "" + integer;
    }
}
