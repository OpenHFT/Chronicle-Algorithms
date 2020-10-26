/*
 *     Copyright (C) 2015-2020 chronicle.software
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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Created by peter on 14/09/15.
 * For a post of customizing hashing strategies.
 */
public class HashTesterRunner implements Runnable {
    private final Class testerMainClass;

    public HashTesterRunner(Class testerMainClass) {
        this.testerMainClass = testerMainClass;
    }

    private static void performTest(HashTest hashTest, Method method) {
        AtomicLong counter = new AtomicLong();
        Set set = new HashSet();
        Consumer consumer = o -> {
            set.add(o);
            counter.incrementAndGet();
        };
        try {
            method.invoke(null, consumer);

            System.out.println(hashTest.value() + ": { hashes: " + counter + ", collisions: " + (counter.get() - set.size()) + " }");
        } catch (Exception e) {
            System.err.println(hashTest.value() + ": Failed");
            e.printStackTrace();
        }
    }

    public static int performTest(Consumer<Consumer> consumer2) {
        AtomicInteger counter = new AtomicInteger();
        Set set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Consumer consumer = o -> {
            set.add(o);
            counter.incrementAndGet();
        };

        consumer2.accept(consumer);
        return (counter.get() - set.size());
    }

    @Override
    public void run() {
        for (Method method : testerMainClass.getDeclaredMethods()) {
            HashTest hashTest = method.getAnnotation(HashTest.class);
            if (hashTest != null)
                performTest(hashTest, method);
        }
    }
}
