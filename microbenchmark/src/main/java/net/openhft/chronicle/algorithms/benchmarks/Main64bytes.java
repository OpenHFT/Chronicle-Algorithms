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

import net.openhft.affinity.Affinity;
import net.openhft.chronicle.algo.hashing.LongHashFunction;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.algo.OptimisedBytesHash;
import net.openhft.chronicle.core.Jvm;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

/**
 * Created by peter on 11/08/15.
 */
@State(Scope.Thread)
public class Main64bytes {
    final Bytes bytes = Bytes.allocateDirect(64).unchecked(true);
    final ByteBuffer buffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
    long num = 0;
    LongHashFunction city_1_1 = LongHashFunction.city_1_1();
    LongHashFunction murmur_3 = LongHashFunction.murmur_3();

    public static void main(String... args) throws RunnerException, InvocationTargetException, IllegalAccessException {
        Affinity.setAffinity(2);
        if (Jvm.isDebug()) {
            Main64bytes main = new Main64bytes();
            for (Method m : Main64bytes.class.getMethods()) {
                if (m.getAnnotation(Benchmark.class) != null) {
                    m.invoke(main);
                }
            }
        } else {
            int time = Boolean.getBoolean("longTest") ? 30 : 2;
            System.out.println("measurementTime: " + time + " secs");
            Options opt = new OptionsBuilder()
                    .include(Main64bytes.class.getSimpleName())
                    .mode(Mode.SampleTime)
                    .measurementTime(TimeValue.seconds(time))
                    .timeUnit(TimeUnit.NANOSECONDS)
                    .forks(1)
                    .build();

            new Runner(opt).run();
        }
    }

    @Benchmark
    public long vanillaHash() {
        for (int i = 0; i < bytes.capacity(); i += 8)
            bytes.writeLong(i, num += 0x1111111111111111L);
        return OptimisedBytesHash.INSTANCE.applyAsLong(bytes);
    }

    @Benchmark
    public long city11Hash() {
        for (int i = 0; i < buffer.capacity(); i += 8)
            buffer.putLong(i, num += 0x1111111111111111L);
        return city_1_1.hashBytes(buffer);
    }

    @Benchmark
    public long murmur3Hash() {
        for (int i = 0; i < buffer.capacity(); i += 8)
            buffer.putLong(i, num += 0x1111111111111111L);
        return murmur_3.hashBytes(buffer);
    }
}

