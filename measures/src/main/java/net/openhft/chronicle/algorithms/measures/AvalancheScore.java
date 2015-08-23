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

/**
 * Created by peter on 22/08/15.
 */
public class AvalancheScore {
    /**
     * Based on the SMHasher Avalanche test
     * <p>
     * search for biases bits.
     * when flipping a single bit of the input, the output should have a 49% - 51% chance of flipping. Some randomness is expected.
     *
     * @return the worst flip bias.
     */
    public static double score(AddressWrapper wrapper) {
        int runs = 1000;
        int bits = 8192;
        Double[] scores = new Double[runs];
        for (int t = 0; t < runs; t++) {
            int[] bitFlipCount = new int[64];
            long[] hashs = new long[bits];
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
                hashs[i] = wrapper.hash();
                b.writeLong(index, prev);
            }
            for (int i = 0; i < hashs.length - 1; i++) {
                long diff = hashs[i + 1] ^ hashs[i];
                for (int k = 0; k < 64; k++) {
                    bitFlipCount[k] += (int) ((diff >>> k) & 1);
                }
            }
            long tests = bits - 1;
            double sum = 0;
            for (int i = 0; i < bitFlipCount.length; i++) {
                int count = bitFlipCount[i];
                double bitScore = 10000L * count / tests / 100.0;
                double err = Math.abs(bitScore - 50);
                sum += err;
            }
            scores[t] = sum / bitFlipCount.length;
        }
        Arrays.sort(scores);

        double score = scores[runs * 99 / 100];
        System.out.printf("Avalanche: The 99%%tile of the drift from 50%% was %.2f%%%n", score);
        return score;
    }
}
