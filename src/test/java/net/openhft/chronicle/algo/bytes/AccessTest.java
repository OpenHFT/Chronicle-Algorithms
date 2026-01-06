/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AccessTest {

    private Access<Object> access;
    private Object handle;

    @BeforeEach
    void setUp() {
        access = mock(Access.class);
        handle = new Object();
    }

    @Test
    void testCompareAndSwapInt() {
        long offset = 0L;
        int expected = 10;
        int value = 20;

        when(access.compareAndSwapInt(handle, offset, expected, value)).thenReturn(true);

        assertTrue(access.compareAndSwapInt(handle, offset, expected, value), "compareAndSwapInt succeeds");
        verify(access).compareAndSwapInt(handle, offset, expected, value);
    }

    @Test
    void testCompareAndSwapLong() {
        long offset = 0L;
        long expected = 10L;
        long value = 20L;

        when(access.compareAndSwapLong(handle, offset, expected, value)).thenReturn(true);

        assertTrue(access.compareAndSwapLong(handle, offset, expected, value), "compareAndSwapLong succeeds");
        verify(access).compareAndSwapLong(handle, offset, expected, value);
    }
}
