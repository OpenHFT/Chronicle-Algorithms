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

package net.openhft.chronicle.algo.hashing;

import net.openhft.chronicle.algo.bytes.NativeAccess;
import net.openhft.chronicle.bytes.NativeBytes;
import org.junit.Ignore;
import org.junit.Test;

import java.security.SecureRandom;

/**
 * Created by peter on 28/06/15.
 */
public class MurmurHash3MoreTest {
    @Test
    @Ignore("Long running, avg score = 6994")
    public void testSmallRandomness() {
        long time = 0, timeCount = 0;
        long scoreSum = 0;
        for (int t = 1; t < 500; t++) {
            long[] hashs = new long[8192];
            NativeBytes b = NativeBytes.nativeBytes(8);
            for (int i = 0; i < hashs.length; i++) {
                b.clear();
                b.append(t);
                b.append('-');
                b.append(i);
                long start = System.nanoTime();
                hashs[i] = LongHashFunction.murmur_3().hash((Object) null, NativeAccess.instance(), b.address(b.readPosition()), b.readRemaining());
                time += System.nanoTime() - start;
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
            scoreSum += score;
            if (t % 50 == 0)
                System.out.println(t + " - Score: " + score);
        }
        System.out.println("Average score: " + scoreSum / 500);
        System.out.printf("Average time %.3f us%n", time / timeCount / 1e3);
    }

    @Ignore("Long running, avg score = 6836")
    @Test
    public void testRandomness() {
        long time = 0, timeCount = 0;
        long scoreSum = 0;
        for (int t = 0; t < 500; t++) {
            long[] hashs = new long[8192];
            NativeBytes b = NativeBytes.nativeBytes(hashs.length / 64);
            byte[] init = new byte[hashs.length / 64];
            new SecureRandom().nextBytes(init);
            for (int i = 0; i < hashs.length; i++) {
                b.clear();
                b.write(init);

                b.writeLong(i >> 6 << 3, 1L << i);
                b.readLimit(hashs.length / 8);
                long start = System.nanoTime();
                hashs[i] = LongHashFunction.murmur_3().hash((Object) null, NativeAccess.instance(), b.address(b.readPosition()), b.readRemaining());
                time += System.nanoTime() - start;
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
            scoreSum += score;
            if (t % 50 == 0)
                System.out.println(t + " - Score: " + score);
        }
        System.out.println("Average score: " + scoreSum / 500);
        System.out.printf("Average time %.3f us%n", time / timeCount / 1e3);
    }
}


