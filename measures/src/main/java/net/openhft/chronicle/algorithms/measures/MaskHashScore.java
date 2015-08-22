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
            new SecureRandom().nextBytes(init);
            // low bit count test
            if (t % 2 == 0) {
                byte[] init2 = new byte[bits / 8];
                new SecureRandom().nextBytes(init2);
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
        System.out.println("MaskHashScore 99%tile collisions: " + score);
        return score;
    }
}
