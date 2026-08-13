package com.group4.library.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static String nextReaderId() {
        return String.format("R%03d", counter.incrementAndGet());
    }
}
