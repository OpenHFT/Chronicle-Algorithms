/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AbstractReadWriteLockingStrategyTest {

    private AbstractReadWriteLockingStrategy strategy;
    private Access<Object> access;
    private Object handle;

    @BeforeEach
    void setUp() {
        strategy = Mockito.mock(AbstractReadWriteLockingStrategy.class, Mockito.CALLS_REAL_METHODS);
        access = Mockito.mock(Access.class);
        handle = new Object();
    }

    @Test
    void testTryLock() {
        when(strategy.tryWriteLock(access, handle, 0L)).thenReturn(true);
        boolean result = strategy.tryLock(access, handle, 0L);
        assertTrue(result);
        verify(strategy).tryWriteLock(access, handle, 0L);
    }

    @Test
    void testUnlock() {
        doNothing().when(strategy).writeUnlock(access, handle, 0L);
        strategy.unlock(access, handle, 0L);
        verify(strategy).writeUnlock(access, handle, 0L);
    }

    @Test
    void testIsReadLocked() {
        when(strategy.readLockCount(1L)).thenReturn(1);
        assertTrue(strategy.isReadLocked(1L));

        when(strategy.readLockCount(0L)).thenReturn(0);
        assertFalse(strategy.isReadLocked(0L));
    }
}
