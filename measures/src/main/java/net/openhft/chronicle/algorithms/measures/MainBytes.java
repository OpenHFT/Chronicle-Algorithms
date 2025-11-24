/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algorithms.measures;

import net.openhft.affinity.Affinity;
import net.openhft.chronicle.algo.hashing.LongHashFunction;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.algo.OptimisedBytesStoreHash;
import net.openhft.chronicle.core.Jvm;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * JMH microbenchmark that compares different hashing strategies over {@link Bytes} payloads.
 *
 * <p>The benchmark fills a direct {@link Bytes} instance with pseudo random 64 bit values and
 * measures the throughput and latency of several hashing functions, including
 * {@link OptimisedBytesStoreHash}, {@link LongHashFunction#city_1_1()}, {@link LongHashFunction#murmur_3()},
 * and {@link LongHashFunction#xx_r39()}. Payload size is controlled by the {@link #size} parameter.
 *
 * <p>When run under a debugger ({@link Jvm#isDebug()} is true) the benchmark methods are invoked
 * directly. In normal operation the {@link Runner} entry point is used to execute the configured
 * JMH benchmarks.
 */
@State(Scope.Thread)
public class MainBytes {
    static final LongHashFunction city_1_1 = LongHashFunction.city_1_1();
    static final LongHashFunction murmur_3 = LongHashFunction.murmur_3();
    static final LongHashFunction xx_r39 = LongHashFunction.xx_r39();
    Bytes bytes;
    long num = 0;
    @Param({"16", "64", "256"})
    int size;

    public static void main(String... args)
            throws RunnerException, InvocationTargetException, IllegalAccessException {
        Affinity.setAffinity(2);
        if (Jvm.isDebug()) {
            MainBytes main = new MainBytes();
            main.size = 16;
            main.fillBytes();
            for (Method m : MainBytes.class.getMethods()) {
                if (m.getAnnotation(Benchmark.class) != null) {
                    m.invoke(main);
                }
            }
        } else {
            int time = Jvm.getBoolean("longTest") ? 30 : 2;
            System.out.println("measurementTime: " + time + " secs");
            Options opt = new OptionsBuilder()
                    .include(MainBytes.class.getSimpleName())
                    .mode(Mode.SampleTime)
                    .measurementTime(TimeValue.seconds(time))
                    .timeUnit(TimeUnit.NANOSECONDS)
                    .forks(1)
                    .build();

            new Runner(opt).run();
        }
    }

    @Setup(Level.Trial)
    public void fillBytes() {
        bytes = Bytes.allocateDirect(size).unchecked(true);
        for (int i = 0; i < bytes.capacity(); i += 8) {
            bytes.writeLong(i, num += 0x1111111111111111L);
        }
        bytes.writePosition(bytes.capacity());
    }

    @Benchmark
    public long vanillaHash() {
        return OptimisedBytesStoreHash.INSTANCE.applyAsLong(bytes);
    }

    @Benchmark
    public long city11Hash() {
        return city_1_1.hashMemory(bytes.address(bytes.readPosition()), bytes.readRemaining());
    }

    @Benchmark
    public long murmur3Hash() {
        return murmur_3.hashMemory(bytes.address(bytes.readPosition()), bytes.readRemaining());
    }

    @Benchmark
    public long xx39Hash() {
        return xx_r39.hashMemory(bytes.address(bytes.readPosition()), bytes.readRemaining());
    }
}
