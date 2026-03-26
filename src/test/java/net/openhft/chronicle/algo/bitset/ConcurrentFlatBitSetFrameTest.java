/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import net.openhft.chronicle.algo.bytes.Access;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class ConcurrentFlatBitSetFrameTest {
    private ConcurrentFlatBitSetFrame bitSetFrame;
    private Access<Object> access;
    private Object handle;
    private long offset;

    @BeforeEach
    public void setUp() {
        bitSetFrame = new ConcurrentFlatBitSetFrame(64);
        access = mock(Access.class);
        handle = new Object();
        offset = 0L;
    }

    @Test
    public void testFlip() {
        long bitIndex = 5L;
        long byteIndex = bitIndex / 64;
        long mask = 1L << bitIndex;

        when(access.readVolatileLong(handle, byteIndex)).thenReturn(0L).thenReturn(mask);
        when(access.compareAndSwapLong(handle, byteIndex, 0L, mask)).thenReturn(true);

        bitSetFrame.flip(access, handle, offset, bitIndex);
        verify(access, times(1)).compareAndSwapLong(handle, byteIndex, 0L, mask);

        when(access.readVolatileLong(handle, byteIndex)).thenReturn(mask).thenReturn(0L);
        when(access.compareAndSwapLong(handle, byteIndex, mask, 0L)).thenReturn(true);

        bitSetFrame.flip(access, handle, offset, bitIndex);
        verify(access, times(1)).compareAndSwapLong(handle, byteIndex, mask, 0L);
    }

    @Test
    public void testSet() {
        long bitIndex = 5L;
        long byteIndex = bitIndex / 64;
        long mask = 1L << bitIndex;

        when(access.readVolatileLong(handle, byteIndex)).thenReturn(0L).thenReturn(mask);
        when(access.compareAndSwapLong(handle, byteIndex, 0L, mask)).thenReturn(true);

        bitSetFrame.set(access, handle, offset, bitIndex);
        verify(access, times(1)).compareAndSwapLong(handle, byteIndex, 0L, mask);
    }

    @Test
    public void testSetNextNContinuousClearBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        long byteIndex = fromIndex / 64;
        long mask = 7L << fromIndex;

        when(access.readVolatileLong(handle, byteIndex)).thenReturn(0L);
        when(access.compareAndSwapLong(handle, byteIndex, 0L, mask)).thenReturn(true);

        assertEquals(5L, bitSetFrame.setNextNContinuousClearBits(access, handle, offset, fromIndex, numberOfBits));
        verify(access, times(1)).compareAndSwapLong(handle, byteIndex, 0L, mask);
    }

    @Test
    public void testClearNextNContinuousSetBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        long byteIndex = fromIndex / 64;
        long mask = ~(7L << fromIndex);

        when(access.readVolatileLong(handle, byteIndex)).thenReturn(~0L);
        when(access.compareAndSwapLong(handle, byteIndex, ~0L, mask)).thenReturn(true);

        assertEquals(5L, bitSetFrame.clearNextNContinuousSetBits(access, handle, offset, fromIndex, numberOfBits));
        verify(access, times(1)).compareAndSwapLong(handle, byteIndex, ~0L, mask);
    }
}