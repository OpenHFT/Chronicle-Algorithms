/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AbstractReadWriteLockStateTest {

    private AbstractReadWriteLockState lockState;

    @BeforeEach
    void setUp() {
        lockState = Mockito.mock(AbstractReadWriteLockState.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    void testTryLock() {
        when(lockState.tryWriteLock()).thenReturn(true);
        boolean result = lockState.tryLock();
        assertTrue(result);
        verify(lockState).tryWriteLock();
    }

    @Test
    void testUnlock() {
        doNothing().when(lockState).writeUnlock();
        lockState.unlock();
        verify(lockState).writeUnlock();
    }
}
