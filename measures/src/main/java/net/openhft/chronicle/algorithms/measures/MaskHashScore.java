/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algorithms.measures;

import net.openhft.chronicle.bytes.NativeBytes;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by peter on 22/08/15.
 */
public class MaskHashScore {
    /**
     * This test looks at how many collision you get in the lower bits.
     * It generates 8K hashes, for 8Kbit input and look at at the lower 14 bits (for 16K values)
     * The ideal is 8K unique hashes after mask.
     */
    public static double score(AddressWrapper wrapper) {
        int runs = 2000;

        int bits = 8192;
        int mask = bits * 2 - 1;
        int[] collisions = new int[runs];
        for (int t = 0; t < runs; t++) {
            Set<Integer> maskedhashs = new HashSet<>();
            byte[] init = new byte[bits / 8];
            NativeBytes b = NativeBytes.nativeBytes(init.length);
            SecureRandom rand = new SecureRandom();
            rand.nextBytes(init);
            // low bit count test
            if (t % 2 == 0) {
                byte[] init2 = new byte[bits / 8];
                rand.nextBytes(init2);
                for (int i = 0; i < init.length; i++)
                    init[i] &= init2[i];
            }
            wrapper.setAddress(b.address(0), b.realCapacity());

            b.clear();
            b.write(init);
            for (int i = 0; i < bits; i++) {
                int index = i >> 6 << 3;
                long prev = b.readLong(index);
                b.writeLong(index, prev ^ (1L << i));
                b.readLimit(bits / 8);
                maskedhashs.add((int) (wrapper.hash() & mask));
                b.writeLong(index, prev);
            }
            collisions[t] = (bits - maskedhashs.size());
        }
        Arrays.sort(collisions);
        int score = collisions[runs * 99 / 100];
        System.out.println("Mask of Hash: 99%tile collisions: " + score);
        return score;
    }
}
