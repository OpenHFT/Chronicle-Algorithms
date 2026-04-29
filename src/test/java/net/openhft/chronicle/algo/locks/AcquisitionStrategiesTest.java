/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AcquisitionStrategiesTest {

    private LockingStrategy lockingStrategy;
    private ReadWriteWithWaitsLockingStrategy rwWithWaitsStrategy;
    private Access<Object> access;
    private Object handle;

    @BeforeEach
    void setUp() {
        lockingStrategy = mock(LockingStrategy.class);
        rwWithWaitsStrategy = mock(ReadWriteWithWaitsLockingStrategy.class);
        access = mock(Access.class);
        handle = new Object();
    }

    @Test
    void testSpinLoopAcquisitionStrategy() {
        AcquisitionStrategy<LockingStrategy, RuntimeException> strategy =
                AcquisitionStrategies.spinLoop(100, TimeUnit.MILLISECONDS);

        TryAcquireOperation<LockingStrategy> operation = new TryAcquireOperation<LockingStrategy>() {
            @Override
            public <T> boolean tryAcquire(LockingStrategy strategy, Access<T> access, T t, long offset) {
                return true;
            }
        };

        boolean result = strategy.acquire(operation::tryAcquire, lockingStrategy, access, handle, 0L);
        assertTrue(result);
    }

    @Test
    void testSpinLoopOrFailAcquisitionStrategy() {
        AcquisitionStrategy<LockingStrategy, RuntimeException> strategy =
                AcquisitionStrategies.spinLoopOrFail(100, TimeUnit.MILLISECONDS);

        TryAcquireOperation<LockingStrategy> operation = new TryAcquireOperation<LockingStrategy>() {
            @Override
            public <T> boolean tryAcquire(LockingStrategy strategy, Access<T> access, T t, long offset) {
                return false;
            }
        };

        assertThrows(IllegalStateException.class, () ->
                strategy.acquire(operation::tryAcquire, lockingStrategy, access, handle, 0L));
    }

    @Test
    void testSpinLoopRegisteringWaitOrFailAcquisitionStrategy() {
        AcquisitionStrategy<ReadWriteWithWaitsLockingStrategy, RuntimeException> strategy =
                AcquisitionStrategies.spinLoopRegisteringWaitOrFail(100, TimeUnit.MILLISECONDS);

        TryAcquireOperation<ReadWriteWithWaitsLockingStrategy> operation = new TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>() {
            @Override
            public <T> boolean tryAcquire(ReadWriteWithWaitsLockingStrategy strategy, Access<T> access, T t, long offset) {
                return false;
            }
        };

        doNothing().when(rwWithWaitsStrategy).registerWait(any(), any(), anyLong());
        doNothing().when(rwWithWaitsStrategy).deregisterWait(any(), any(), anyLong());

        assertThrows(IllegalStateException.class, () ->
                strategy.acquire(operation::tryAcquire, rwWithWaitsStrategy, access, handle, 0L));

        verify(rwWithWaitsStrategy).registerWait(any(), any(), anyLong());
        verify(rwWithWaitsStrategy).deregisterWait(any(), any(), anyLong());
    }

    @FunctionalInterface
    interface TryAcquireOperation<S> {
        <T> boolean tryAcquire(S strategy, Access<T> access, T t, long offset);
    }
}