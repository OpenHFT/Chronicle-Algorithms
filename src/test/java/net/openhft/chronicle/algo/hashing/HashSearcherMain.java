/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import java.util.Comparator;
import java.util.Random;

import static net.openhft.chronicle.algo.hashing.HashTesterMain.FTSE;

/**
 * Created by peter on 14/09/15.
 */
public class HashSearcherMain {
    public static void main(String[] args) {
        Random rand = new Random();
        int samples = 1_000_000;
        RandomOptimiser<Integer> optimiser = new RandomOptimiser<>(() -> rand.nextInt() | 1,
                Comparator.comparing(Integer::toUnsignedLong), samples);
        int mask = (1 << 10) - 1;
        optimiser.randomSearch(i ->
                HashTesterRunner.performTest(c ->
                        FTSE.stream().map(s ->
                                hash(s, i) & mask).forEach(c)));

        System.out.println("hash:" + optimiser);

        optimiser.randomSearch(i ->
                HashTesterRunner.performTest(c ->
                        FTSE.stream().map(s ->
                                xorShift16(hash(s, i)) & mask).forEach(c)));

        System.out.println("xorShift16(hash):" + optimiser);

        optimiser.randomSearch(i ->
                HashTesterRunner.performTest(c ->
                        FTSE.stream().map(s ->
                                addShift16(hash(s, i)) & mask).forEach(c)));

        System.out.println("addShift16(hash):" + optimiser);

        optimiser.randomSearch(i ->
                HashTesterRunner.performTest(c ->
                        FTSE.stream().map(s ->
                                xorShift16n9(hash(s, i)) & mask).forEach(c)));
        System.out.println("xorShift16n9(hash): " + optimiser);
    }

    private static int hash(String s, int multiplier) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = multiplier * h + s.charAt(i);
        }
        return h;
    }

    private static int xorShift16(int hash) {
        return hash ^ (hash >> 16);
    }

    private static int addShift16(int hash) {
        return hash + (hash >> 16);
    }

    private static int xorShift16n9(int hash) {
        hash ^= (hash >>> 16);
        hash ^= (hash >>> 9);
        return hash;
    }
}
