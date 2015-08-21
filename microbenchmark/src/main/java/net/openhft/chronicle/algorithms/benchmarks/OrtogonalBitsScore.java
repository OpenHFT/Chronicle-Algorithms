/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.algorithms.benchmarks;

import net.openhft.chronicle.bytes.NativeBytes;

import java.security.SecureRandom;

/**
 * Created by peter on 21/08/15.
 */
public class OrtogonalBitsScore {
    public static long score(AddressWrapper wrapper) {
        long time = 0, timeCount = 0;
        long scoreSum = 0;
        int runs = 500;
        for (int t = 0; t < runs; t++) {
            long[] hashs = new long[8192];
            byte[] init = new byte[hashs.length / 8];
            NativeBytes b = NativeBytes.nativeBytes(init.length);
            new SecureRandom().nextBytes(init);
            // low bit count test
            if (t % 2 == 0) {
                byte[] init2 = new byte[hashs.length / 8];
                new SecureRandom().nextBytes(init2);
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
                time += System.nanoTime() - start;
                b.writeLong(index, prev);
                timeCount++;
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
            scoreSum += score * score;
            if (t % 50 == 0)
                System.out.println(t + " - Score: " + score);
        }
        System.out.println("Average score: " + (long) Math.sqrt(scoreSum / runs));
        System.out.printf("Average time %.3f us%n", time / timeCount / 1e3);
        return scoreSum / runs;
    }
}
