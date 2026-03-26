/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import net.openhft.chronicle.algo.bytes.Access;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class BitSetFrameTest {

    private BitSetFrame bitSetFrame;
    private Access<Object> access;
    private Object handle;
    private long offset;

    @BeforeEach
    void setUp() {
        bitSetFrame = mock(BitSetFrame.class);
        access = mock(Access.class);
        handle = new Object();
        offset = 0L;
    }

    @Test
    void testFlip() {
        long bitIndex = 5L;
        bitSetFrame.flip(access, handle, offset, bitIndex);
        verify(bitSetFrame).flip(access, handle, offset, bitIndex);
    }

    @Test
    void testFlipRange() {
        long fromIndex = 5L;
        long toIndex = 10L;
        bitSetFrame.flipRange(access, handle, offset, fromIndex, toIndex);
        verify(bitSetFrame).flipRange(access, handle, offset, fromIndex, toIndex);
    }

    @Test
    void testSet() {
        long bitIndex = 5L;
        bitSetFrame.set(access, handle, offset, bitIndex);
        verify(bitSetFrame).set(access, handle, offset, bitIndex);
    }

    @Test
    void testSetIfClear() {
        long bitIndex = 5L;
        when(bitSetFrame.setIfClear(access, handle, offset, bitIndex)).thenReturn(true);
        assertTrue(bitSetFrame.setIfClear(access, handle, offset, bitIndex));
        verify(bitSetFrame).setIfClear(access, handle, offset, bitIndex);
    }

    @Test
    void testClearIfSet() {
        long bitIndex = 5L;
        when(bitSetFrame.clearIfSet(access, handle, offset, bitIndex)).thenReturn(true);
        assertTrue(bitSetFrame.clearIfSet(access, handle, offset, bitIndex));
        verify(bitSetFrame).clearIfSet(access, handle, offset, bitIndex);
    }

    @Test
    void testSetWithBooleanValue() {
        long bitIndex = 5L;
        boolean value = true;
        bitSetFrame.set(access, handle, offset, bitIndex, value);
        verify(bitSetFrame).set(access, handle, offset, bitIndex, value);
    }

    @Test
    void testSetRangeWithBooleanValue() {
        long fromIndex = 5L;
        long toIndex = 10L;
        boolean value = true;
        bitSetFrame.setRange(access, handle, offset, fromIndex, toIndex, value);
        verify(bitSetFrame).setRange(access, handle, offset, fromIndex, toIndex, value);
    }

    @Test
    void testIsSet() {
        long bitIndex = 5L;
        when(bitSetFrame.isSet(access, handle, offset, bitIndex)).thenReturn(true);
        assertTrue(bitSetFrame.isSet(access, handle, offset, bitIndex));
        verify(bitSetFrame).isSet(access, handle, offset, bitIndex);
    }

    @Test
    void testIsClear() {
        long bitIndex = 5L;
        when(bitSetFrame.isClear(access, handle, offset, bitIndex)).thenReturn(true);
        assertTrue(bitSetFrame.isClear(access, handle, offset, bitIndex));
        verify(bitSetFrame).isClear(access, handle, offset, bitIndex);
    }
}
