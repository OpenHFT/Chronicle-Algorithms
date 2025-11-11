/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Created by peter on 14/09/15.
 * For a post of customizing hashing strategies.
 */
class HashTesterRunner implements Runnable {
    private final Class<?> testerMainClass;

    public HashTesterRunner(Class<?> testerMainClass) {
        this.testerMainClass = testerMainClass;
    }

    private static void performTest(HashTest hashTest, Method method) {
        AtomicLong counter = new AtomicLong();
        Set<Object> set = new HashSet<>();
        Consumer<?> consumer = o -> {
            set.add(o);
            counter.incrementAndGet();
        };
        try {
            method.invoke(null, consumer);

            System.out.println(hashTest.value() + ": { hashes: " + counter + ", collisions: " + (counter.get() - set.size()) + " }");
        } catch (Exception e) {
            System.err.println(hashTest.value() + ": Failed");
            e.printStackTrace();
        }
    }

    public static int performTest(Consumer<Consumer<Object>> consumer2) {
        AtomicInteger counter = new AtomicInteger();
        Set<Object> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Consumer<Object> consumer = o -> {
            set.add(o);
            counter.incrementAndGet();
        };

        consumer2.accept(consumer);
        return (counter.get() - set.size());
    }

    @Override
    public void run() {
        for (Method method : testerMainClass.getDeclaredMethods()) {
            HashTest hashTest = method.getAnnotation(HashTest.class);
            if (hashTest != null)
                performTest(hashTest, method);
        }
    }
}
