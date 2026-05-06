/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algorithms.measures;

import net.openhft.chronicle.bytes.NativeBytes;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Scores how orthogonal the outputs of a hash function are when single input bits flip.
 * <p>
 * The metric penalises pairs of outputs whose Hamming distance is too small, providing a measure of
 * output independence across nearby inputs.
 */
public class OrtogonalBitsScore {
    /**
     * Execute the orthogonality test and return the 99th percentile penalty score.
     *
     * @param wrapper implementation used to hash a native memory block
     * @return high percentile cumulative penalty; lower values indicate better independence
     */
    public static long score(AddressWrapper wrapper) {
        int runs = 1000;
        long[] scores = new long[runs];
        long[] times = new long[runs];
        for (int t = 0; t < runs; t++) {
            long[] hashs = new long[8192];
            byte[] init = new byte[hashs.length / 8];
            NativeBytes b = NativeBytes.nativeBytes(init.length);
            SecureRandom rand = new SecureRandom();
            rand.nextBytes(init);
            // low bit count test
            if (t % 2 == 0) {
                byte[] init2 = new byte[hashs.length / 8];
                rand.nextBytes(init2);
                for (int i = 0; i < init.length; i++)
                    init[i] &= init2[i];
            }
            wrapper.setAddress(b.address(0), b.realCapacity());

            b.clear();
            b.write(init);
            for (int i = 0; i < hashs.length; i++) {
                int index = i >> 6 << 3;
                long prev = b.readLong(index);
                b.writeLong(index, prev ^ (1L << i));
                b.readLimit(hashs.length / 8);
                long start = System.nanoTime();
                hashs[i] = wrapper.hash();
                times[t] = System.nanoTime() - start;
                b.writeLong(index, prev);
            }
            long score = 0;
            for (int i = 0; i < hashs.length - 1; i++)
                for (int j = i + 1; j < hashs.length; j++) {
                    long diff = hashs[j] ^ hashs[i];
                    int diffBC = Long.bitCount(diff);
                    if (diffBC < 18) {
                        long d = 1L << (17 - diffBC);
                        score += d;
                    }
                }
            scores[t] = score;
        }
        Arrays.sort(scores);
        Arrays.sort(times);
        long score = scores[runs * 99 / 100];
        long time = times[runs * 99 / 100];
        System.out.println("Orthogonal bits: 99%tile score: " + score);
        System.out.printf("Speed: The 99%%tile for latency was %.3f us%n", time / 1e3);
        return score;
    }
}
