/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Created by peter on 14/09/15.
 */
class RandomOptimiser<T> {
    private final Supplier<T> inputSupplier;
    private final Comparator<T> tieBreaker;
    private final int samples;
    private T lowest;
    private int lowestScore = Integer.MAX_VALUE;
    private T highest;
    private int highestScore = Integer.MIN_VALUE;

    public RandomOptimiser(Supplier<T> inputSupplier, Comparator<T> tieBreaker, int samples) {
        this.inputSupplier = inputSupplier;
        this.tieBreaker = tieBreaker;
        this.samples = samples;
    }

    public void randomSearch(Function<T, Integer> test) {
        lowest = highest = null;
        lowestScore = Integer.MAX_VALUE;
        highestScore = Integer.MIN_VALUE;
        IntStream.range(0, samples).parallel().forEach(i -> {
            T num = inputSupplier.get();
            int score = test.apply(num);
            synchronized (this) {
                if (lowestScore > score || (lowestScore == score && tieBreaker.compare(lowest, num) > 0)) {
                    lowestScore = score;
                    lowest = num;
                }
                if (highestScore < score || (highestScore == score && tieBreaker.compare(highest, num) > 0)) {
                    highestScore = score;
                    highest = num;
                }
            }
        });
    }

    @Override
    public String toString() {
        return "RandomOptimiser{" +
                "samples=" + samples +
                ", lowest=" + lowest +
                ", lowestScore=" + lowestScore +
                ", highest=" + highest +
                ", highestScore=" + highestScore +
                '}';
    }
}
