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
