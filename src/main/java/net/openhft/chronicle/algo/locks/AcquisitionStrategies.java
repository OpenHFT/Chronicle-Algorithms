/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

import java.util.concurrent.TimeUnit;

public final class AcquisitionStrategies {

    private AcquisitionStrategies() {
    }

    public static <S extends LockingStrategy>
    AcquisitionStrategy<S, RuntimeException> spinLoop(long duration, TimeUnit unit) {
        return new SpinLoopAcquisitionStrategy<>(duration, unit);
    }

    public static <S extends LockingStrategy>
    AcquisitionStrategy<S, RuntimeException> spinLoopOrFail(long duration, TimeUnit unit) {
        return new SpinLoopOrFailAcquisitionStrategy<>(duration, unit);
    }

    public static <S extends ReadWriteWithWaitsLockingStrategy>
    AcquisitionStrategy<S, RuntimeException> spinLoopRegisteringWaitOrFail(
            long duration, TimeUnit unit) {
        return new SpinLoopWriteWithWaitsAcquisitionStrategy<>(duration, unit);
    }

    private static class SpinLoopAcquisitionStrategy<S extends LockingStrategy>
            implements AcquisitionStrategy<S, RuntimeException> {
        private final long durationNanos;

        private SpinLoopAcquisitionStrategy(long duration, TimeUnit unit) {
            durationNanos = unit.toNanos(duration);
        }

        @Override
        public <T> boolean acquire(TryAcquireOperation<? super S> operation, S strategy,
                                   Access<T> access, T t, long offset) {
            if (operation.tryAcquire(strategy, access, t, offset))
                return true;
            long deadLine = System.currentTimeMillis() + durationNanos;
            beforeLoop(strategy, access, t, offset);
            do {
                if (operation.tryAcquire(strategy, access, t, offset))
                    return true;
            } while (deadLine - System.currentTimeMillis() >= 0L); // overflow-cautious
            afterLoop(strategy, access, t, offset);
            return end();
        }

        <T> void beforeLoop(S strategy, Access<T> access, T t, long offset) {
        }

        <T> void afterLoop(S strategy, Access<T> access, T t, long offset) {
        }

        boolean end() {
            return false;
        }
    }

    private static class SpinLoopOrFailAcquisitionStrategy<S extends LockingStrategy>
            extends SpinLoopAcquisitionStrategy<S> {

        private SpinLoopOrFailAcquisitionStrategy(long duration, TimeUnit unit) {
            super(duration, unit);
        }

        @Override
        boolean end() {
            throw new IllegalStateException("Failed to acquire the lock");
        }
    }

    private static class SpinLoopWriteWithWaitsAcquisitionStrategy<
            S extends ReadWriteWithWaitsLockingStrategy>
            extends SpinLoopOrFailAcquisitionStrategy<S> {

        private SpinLoopWriteWithWaitsAcquisitionStrategy(long duration, TimeUnit unit) {
            super(duration, unit);
        }

        @Override
        <T> void beforeLoop(S strategy, Access<T> access, T t, long offset) {
            strategy.registerWait(access, t, offset);
        }

        @Override
        <T> void afterLoop(S strategy, Access<T> access, T t, long offset) {
            strategy.deregisterWait(access, t, offset);
        }
    }
}
