/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class BitSetTest extends TestCase {
    private BitSet bitSet;

    @BeforeEach
    public void setUp() {
        bitSet = mock(BitSet.class);
    }

    @Test
    public void testFlip() {
        long bitIndex = 5L;
        bitSet.flip(bitIndex);
        verify(bitSet).flip(bitIndex);
    }

    @Test
    public void testFlipRange() {
        long fromIndex = 5L;
        long toIndex = 10L;
        bitSet.flipRange(fromIndex, toIndex);
        verify(bitSet).flipRange(fromIndex, toIndex);
    }

    @Test
    public void testSet() {
        long bitIndex = 5L;
        bitSet.set(bitIndex);
        verify(bitSet).set(bitIndex);
    }

    @Test
    public void testSetIfClear() {
        long bitIndex = 5L;
        when(bitSet.setIfClear(bitIndex)).thenReturn(true);
        assertTrue(bitSet.setIfClear(bitIndex));
        verify(bitSet).setIfClear(bitIndex);
    }

    @Test
    public void testClearIfSet() {
        long bitIndex = 5L;
        when(bitSet.clearIfSet(bitIndex)).thenReturn(true);
        assertTrue(bitSet.clearIfSet(bitIndex));
        verify(bitSet).clearIfSet(bitIndex);
    }

    @Test
    public void testSetWithBooleanValue() {
        long bitIndex = 5L;
        boolean value = true;
        bitSet.set(bitIndex, value);
        verify(bitSet).set(bitIndex, value);
    }

    @Test
    public void testSetRange() {
        long fromIndex = 5L;
        long toIndex = 10L;
        bitSet.setRange(fromIndex, toIndex);
        verify(bitSet).setRange(fromIndex, toIndex);
    }

    @Test
    public void testIsRangeSet() {
        long fromIndex = 5L;
        long toIndex = 10L;
        when(bitSet.isRangeSet(fromIndex, toIndex)).thenReturn(true);
        assertTrue(bitSet.isRangeSet(fromIndex, toIndex));
        verify(bitSet).isRangeSet(fromIndex, toIndex);
    }

    @Test
    public void testSetAll() {
        bitSet.setAll();
        verify(bitSet).setAll();
    }

    @Test
    public void testSetRangeWithBooleanValue() {
        long fromIndex = 5L;
        long toIndex = 10L;
        boolean value = true;
        bitSet.setRange(fromIndex, toIndex, value);
        verify(bitSet).setRange(fromIndex, toIndex, value);
    }

    @Test
    public void testClear() {
        long bitIndex = 5L;
        bitSet.clear(bitIndex);
        verify(bitSet).clear(bitIndex);
    }

    @Test
    public void testClearRange() {
        long fromIndex = 5L;
        long toIndex = 10L;
        bitSet.clearRange(fromIndex, toIndex);
        verify(bitSet).clearRange(fromIndex, toIndex);
    }

    @Test
    public void testClearAll() {
        bitSet.clearAll();
        verify(bitSet).clearAll();
    }

    @Test
    public void testGet() {
        long bitIndex = 5L;
        when(bitSet.get(bitIndex)).thenReturn(true);
        assertTrue(bitSet.get(bitIndex));
        verify(bitSet).get(bitIndex);
    }

    @Test
    public void testIsSet() {
        long bitIndex = 5L;
        when(bitSet.isSet(bitIndex)).thenReturn(true);
        assertTrue(bitSet.isSet(bitIndex));
        verify(bitSet).isSet(bitIndex);
    }

    @Test
    public void testIsClear() {
        long bitIndex = 5L;
        when(bitSet.isClear(bitIndex)).thenReturn(true);
        assertTrue(bitSet.isClear(bitIndex));
        verify(bitSet).isClear(bitIndex);
    }

    @Test
    public void testIsRangeClear() {
        long fromIndex = 5L;
        long toIndex = 10L;
        when(bitSet.isRangeClear(fromIndex, toIndex)).thenReturn(true);
        assertTrue(bitSet.isRangeClear(fromIndex, toIndex));
        verify(bitSet).isRangeClear(fromIndex, toIndex);
    }

    @Test
    public void testNextSetBit() {
        long fromIndex = 5L;
        when(bitSet.nextSetBit(fromIndex)).thenReturn(6L);
        assertEquals(6L, bitSet.nextSetBit(fromIndex));
        verify(bitSet).nextSetBit(fromIndex);
    }

    @Test
    public void testNextClearBit() {
        long fromIndex = 5L;
        when(bitSet.nextClearBit(fromIndex)).thenReturn(6L);
        assertEquals(6L, bitSet.nextClearBit(fromIndex));
        verify(bitSet).nextClearBit(fromIndex);
    }

    @Test
    public void testPreviousSetBit() {
        long fromIndex = 5L;
        when(bitSet.previousSetBit(fromIndex)).thenReturn(4L);
        assertEquals(4L, bitSet.previousSetBit(fromIndex));
        verify(bitSet).previousSetBit(fromIndex);
    }

    @Test
    public void testPreviousClearBit() {
        long fromIndex = 5L;
        when(bitSet.previousClearBit(fromIndex)).thenReturn(4L);
        assertEquals(4L, bitSet.previousClearBit(fromIndex));
        verify(bitSet).previousClearBit(fromIndex);
    }

    @Test
    public void testLogicalSize() {
        when(bitSet.logicalSize()).thenReturn(64L);
        assertEquals(64L, bitSet.logicalSize());
        verify(bitSet).logicalSize();
    }

    @Test
    public void testCardinality() {
        when(bitSet.cardinality()).thenReturn(5L);
        assertEquals(5L, bitSet.cardinality());
        verify(bitSet).cardinality();
    }

    @Test
    public void testSetNextClearBit() {
        long fromIndex = 5L;
        when(bitSet.setNextClearBit(fromIndex)).thenReturn(6L);
        assertEquals(6L, bitSet.setNextClearBit(fromIndex));
        verify(bitSet).setNextClearBit(fromIndex);
    }

    @Test
    public void testClearNextSetBit() {
        long fromIndex = 5L;
        when(bitSet.clearNextSetBit(fromIndex)).thenReturn(6L);
        assertEquals(6L, bitSet.clearNextSetBit(fromIndex));
        verify(bitSet).clearNextSetBit(fromIndex);
    }

    @Test
    public void testSetPreviousClearBit() {
        long fromIndex = 5L;
        when(bitSet.setPreviousClearBit(fromIndex)).thenReturn(4L);
        assertEquals(4L, bitSet.setPreviousClearBit(fromIndex));
        verify(bitSet).setPreviousClearBit(fromIndex);
    }

    @Test
    public void testClearPreviousSetBit() {
        long fromIndex = 5L;
        when(bitSet.clearPreviousSetBit(fromIndex)).thenReturn(4L);
        assertEquals(4L, bitSet.clearPreviousSetBit(fromIndex));
        verify(bitSet).clearPreviousSetBit(fromIndex);
    }

    @Test
    public void testSetNextNContinuousClearBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        when(bitSet.setNextNContinuousClearBits(fromIndex, numberOfBits)).thenReturn(5L);
        assertEquals(5L, bitSet.setNextNContinuousClearBits(fromIndex, numberOfBits));
        verify(bitSet).setNextNContinuousClearBits(fromIndex, numberOfBits);
    }

    @Test
    public void testClearNextNContinuousSetBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        when(bitSet.clearNextNContinuousSetBits(fromIndex, numberOfBits)).thenReturn(5L);
        assertEquals(5L, bitSet.clearNextNContinuousSetBits(fromIndex, numberOfBits));
        verify(bitSet).clearNextNContinuousSetBits(fromIndex, numberOfBits);
    }

    @Test
    public void testSetPreviousNContinuousClearBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        when(bitSet.setPreviousNContinuousClearBits(fromIndex, numberOfBits)).thenReturn(2L);
        assertEquals(2L, bitSet.setPreviousNContinuousClearBits(fromIndex, numberOfBits));
        verify(bitSet).setPreviousNContinuousClearBits(fromIndex, numberOfBits);
    }

    @Test
    public void testClearPreviousNContinuousSetBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        when(bitSet.clearPreviousNContinuousSetBits(fromIndex, numberOfBits)).thenReturn(2L);
        assertEquals(2L, bitSet.clearPreviousNContinuousSetBits(fromIndex, numberOfBits));
        verify(bitSet).clearPreviousNContinuousSetBits(fromIndex, numberOfBits);
    }
}
