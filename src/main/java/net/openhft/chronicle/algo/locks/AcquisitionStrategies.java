/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.core.Jvm;

import java.util.concurrent.TimeUnit;

/**
 * Factory for common lock acquisition strategies including spin loops and wait-registration
 * helpers. Designed to be combined with {@link LockingStrategy} implementations.
 */
public final class AcquisitionStrategies {

    // Private constructor to prevent instantiation
    private AcquisitionStrategies() {
    }

    /**
     * Creates an acquisition strategy that spins in a loop until the lock is acquired
     * or the specified duration has passed.
     *
     * @param duration the duration to spin
     * @param unit     the time unit of the duration
     * @param <S>      the type of the locking strategy
     * @return an acquisition strategy that spins in a loop
     */
    public static <S extends LockingStrategy>
    AcquisitionStrategy<S, RuntimeException> spinLoop(long duration, TimeUnit unit) {
        return new SpinLoopAcquisitionStrategy<>(duration, unit);
    }

    /**
     * Creates an acquisition strategy that spins in a loop until the lock is acquired,
     * fails and throws an exception if the specified duration has passed.
     *
     * @param duration the duration to spin
     * @param unit     the time unit of the duration
     * @param <S>      the type of the locking strategy
     * @return an acquisition strategy that spins in a loop or fails
     */
    public static <S extends LockingStrategy>
    AcquisitionStrategy<S, RuntimeException> spinLoopOrFail(long duration, TimeUnit unit) {
        return new SpinLoopOrFailAcquisitionStrategy<>(duration, unit);
    }

    /**
     * Creates an acquisition strategy that spins in a loop, registers a wait before spinning,
     * and deregisters the wait after spinning.
     * Fails and throws an exception if the specified duration has passed.
     *
     * @param duration the duration to spin
     * @param unit     the time unit of the duration
     * @param <S>      the type of the read-write locking strategy
     * @return an acquisition strategy that spins in a loop, registers a wait, or fails
     */
    public static <S extends ReadWriteWithWaitsLockingStrategy>
    AcquisitionStrategy<S, RuntimeException> spinLoopRegisteringWaitOrFail(
            long duration, TimeUnit unit) {
        return new SpinLoopWriteWithWaitsAcquisitionStrategy<>(duration, unit);
    }

    /**
     * Acquisition strategy that spins in a loop until the lock is acquired or the specified
     * duration has passed.
     *
     * @param <S> the type of the locking strategy
     */
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
            long deadLineNanos = System.nanoTime() + durationNanos;
            beforeLoop(strategy, access, t, offset);
            do {
                if (operation.tryAcquire(strategy, access, t, offset))
                    return true;
                Jvm.nanoPause();
            } while (deadLineNanos - System.nanoTime() >= 0L); // overflow-cautious
            afterLoop(strategy, access, t, offset);
            return end();
        }

        /**
         * Method to be overridden for actions before the spin loop starts.
         *
         * @param strategy the locking strategy
         * @param access   the access
         * @param t        the target
         * @param offset   the offset
         * @param <T>      the type of the target
         */
        <T> void beforeLoop(S strategy, Access<T> access, T t, long offset) {
        }

        /**
         * Method to be overridden for actions after the spin loop ends.
         *
         * @param strategy the locking strategy
         * @param access   the access
         * @param t        the target
         * @param offset   the offset
         * @param <T>      the type of the target
         */
        <T> void afterLoop(S strategy, Access<T> access, T t, long offset) {
        }

        /**
         * Indicates the end of the acquisition attempt.
         *
         * @return false to indicate failure to acquire the lock
         */
        boolean end() {
            return false;
        }
    }

    /**
     * Acquisition strategy that spins in a loop until the lock is acquired,
     * fails and throws an exception if the specified duration has passed.
     *
     * @param <S> the type of the locking strategy
     */
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

    /**
     * Acquisition strategy that spins in a loop, registers a wait before spinning,
     * and deregisters the wait after spinning. Fails and throws an exception if the
     * specified duration has passed.
     *
     * @param <S> the type of the read-write locking strategy
     */
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
