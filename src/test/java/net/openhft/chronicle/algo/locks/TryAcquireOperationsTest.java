/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TryAcquireOperationsTest {

    private LockingStrategy lockingStrategy;
    private ReadWriteLockingStrategy readWriteLockingStrategy;
    private ReadWriteWithWaitsLockingStrategy rwWithWaitsLockingStrategy;
    private ReadWriteUpdateLockingStrategy readWriteUpdateLockingStrategy;
    private ReadWriteUpdateWithWaitsLockingStrategy rwUpdateWithWaitsLockingStrategy;
    private Access<Object> access;
    private Object handle;

    @BeforeEach
    void setUp() {
        lockingStrategy = mock(LockingStrategy.class);
        readWriteLockingStrategy = mock(ReadWriteLockingStrategy.class);
        rwWithWaitsLockingStrategy = mock(ReadWriteWithWaitsLockingStrategy.class);
        readWriteUpdateLockingStrategy = mock(ReadWriteUpdateLockingStrategy.class);
        rwUpdateWithWaitsLockingStrategy = mock(ReadWriteUpdateWithWaitsLockingStrategy.class);
        access = mock(Access.class);
        handle = new Object();
    }

    @Test
    void testLock() {
        TryAcquireOperation<LockingStrategy> operation = TryAcquireOperations.lock();
        when(lockingStrategy.tryLock(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(lockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(lockingStrategy).tryLock(access, handle, 0L);
    }

    @Test
    void testReadLock() {
        TryAcquireOperation<ReadWriteLockingStrategy> operation = TryAcquireOperations.readLock();
        when(readWriteLockingStrategy.tryReadLock(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(readWriteLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(readWriteLockingStrategy).tryReadLock(access, handle, 0L);
    }

    @Test
    void testUpgradeReadToWriteLock() {
        TryAcquireOperation<ReadWriteLockingStrategy> operation = TryAcquireOperations.upgradeReadToWriteLock();
        when(readWriteLockingStrategy.tryUpgradeReadToWriteLock(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(readWriteLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(readWriteLockingStrategy).tryUpgradeReadToWriteLock(access, handle, 0L);
    }

    @Test
    void testWriteLock() {
        TryAcquireOperation<ReadWriteLockingStrategy> operation = TryAcquireOperations.writeLock();
        when(readWriteLockingStrategy.tryWriteLock(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(readWriteLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(readWriteLockingStrategy).tryWriteLock(access, handle, 0L);
    }

    @Test
    void testUpgradeReadToWriteLockAndDeregisterWait() {
        TryAcquireOperation<ReadWriteWithWaitsLockingStrategy> operation = TryAcquireOperations.upgradeReadToWriteLockAndDeregisterWait();
        when(rwWithWaitsLockingStrategy.tryUpgradeReadToWriteLockAndDeregisterWait(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(rwWithWaitsLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(rwWithWaitsLockingStrategy).tryUpgradeReadToWriteLockAndDeregisterWait(access, handle, 0L);
    }

    @Test
    void testWriteLockAndDeregisterWait() {
        TryAcquireOperation<ReadWriteWithWaitsLockingStrategy> operation = TryAcquireOperations.writeLockAndDeregisterWait();
        when(rwWithWaitsLockingStrategy.tryWriteLockAndDeregisterWait(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(rwWithWaitsLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(rwWithWaitsLockingStrategy).tryWriteLockAndDeregisterWait(access, handle, 0L);
    }

    @Test
    void testUpdateLock() {
        TryAcquireOperation<ReadWriteUpdateLockingStrategy> operation = TryAcquireOperations.updateLock();
        when(readWriteUpdateLockingStrategy.tryUpdateLock(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(readWriteUpdateLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(readWriteUpdateLockingStrategy).tryUpdateLock(access, handle, 0L);
    }

    @Test
    void testUpgradeReadToUpdateLock() {
        TryAcquireOperation<ReadWriteUpdateLockingStrategy> operation = TryAcquireOperations.upgradeReadToUpdateLock();
        when(readWriteUpdateLockingStrategy.tryUpgradeReadToUpdateLock(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(readWriteUpdateLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(readWriteUpdateLockingStrategy).tryUpgradeReadToUpdateLock(access, handle, 0L);
    }

    @Test
    void testUpgradeUpdateToWriteLock() {
        TryAcquireOperation<ReadWriteUpdateLockingStrategy> operation = TryAcquireOperations.upgradeUpdateToWriteLock();
        when(readWriteUpdateLockingStrategy.tryUpgradeUpdateToWriteLock(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(readWriteUpdateLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(readWriteUpdateLockingStrategy).tryUpgradeUpdateToWriteLock(access, handle, 0L);
    }

    @Test
    void testUpgradeUpdateToWriteLockAndDeregisterWait() {
        TryAcquireOperation<ReadWriteUpdateWithWaitsLockingStrategy> operation = TryAcquireOperations.upgradeUpdateToWriteLockAndDeregisterWait();
        when(rwUpdateWithWaitsLockingStrategy.tryUpgradeUpdateToWriteLockAndDeregisterWait(access, handle, 0L)).thenReturn(true);

        boolean result = operation.tryAcquire(rwUpdateWithWaitsLockingStrategy, access, handle, 0L);
        assertTrue(result);
        verify(rwUpdateWithWaitsLockingStrategy).tryUpgradeUpdateToWriteLockAndDeregisterWait(access, handle, 0L);
    }
}
