/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class VanillaReadWriteWithWaitsLockingStrategyTest {

    private Access<Object> access;
    private Object handle;
    private ReadWriteWithWaitsLockingStrategy strategy;

    @BeforeEach
    void setUp() {
        access = mock(Access.class);
        handle = new Object();
        strategy = VanillaReadWriteWithWaitsLockingStrategy.instance();
    }

    @Test
    void testRwReadLocked() {
        long lock = 5L;
        int expectedReadLocks = 5;
        int readLocks = VanillaReadWriteWithWaitsLockingStrategy.rwReadLocked(lock);
        assertEquals(expectedReadLocks, readLocks, "rwReadLocked extracts read lock count");
    }

    @Test
    void testIsWriteLocked() {
        long state = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_LOCKED;
        assertTrue(strategy.isWriteLocked(state), "RW_WRITE_LOCKED is write locked");

        long nonWriteLockedState = 0;
        assertFalse(strategy.isWriteLocked(nonWriteLockedState), "zero state is not write locked");
    }

    @Test
    void testReadLockCount() {
        long state = 7L; // 7 read locks
        int expectedReadLockCount = 7;
        int readLockCount = strategy.readLockCount(state);
        assertEquals(expectedReadLockCount, readLockCount, "readLockCount extracts read lock count");
    }

    @Test
    void testRegisterWait() {
        long offset = 0L;
        long lock = 0L;
        when(access.readLong(handle, offset)).thenReturn(lock);
        when(access.compareAndSwapLong(handle, offset, lock, lock + VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_WAITING)).thenReturn(true);

        strategy.registerWait(access, handle, offset);
        verify(access).compareAndSwapLong(handle, offset, lock, lock + VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_WAITING);
    }

    @Test
    void testWaitCount() {
        long state = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_WAITING * 3; // 3 waits
        int expectedWaitCount = 3;
        int waitCount = strategy.waitCount(state);
        assertEquals(expectedWaitCount, waitCount, "waitCount extracts wait count");
    }

    @Test
    void testIsLocked() {
        long lockedState = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_LOCKED;
        assertTrue(strategy.isLocked(lockedState), "RW_WRITE_LOCKED is locked");

        long nonLockedState = 0;
        assertFalse(strategy.isLocked(nonLockedState), "zero state is not locked");
    }

    @Test
    void testLockCount() {
        long state = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_LOCKED + 3; // 3 read locks + 1 write lock
        int expectedLockCount = 4;

        int lockCount = strategy.lockCount(state);
        assertEquals(expectedLockCount, lockCount, "lockCount includes read locks and write lock");
    }

    @Test
    void testToString() {
        long state = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_WAITING + 3; // 3 read locks, 1 wait
        String expectedString = "[read locks = 3, write locked = false, waits = 1]";

        String lockStateString = strategy.toString(state);
        assertEquals(expectedString, lockStateString, "toString formats state");
    }

    @Test
    void testSizeInBytes() {
        int expectedSize = 8;
        assertEquals(expectedSize, strategy.sizeInBytes(), "sizeInBytes returns 8");
    }
}
