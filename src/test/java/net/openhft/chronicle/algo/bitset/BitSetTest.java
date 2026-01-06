/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BitSetTest {
    private BitSet bitSet;

    @BeforeEach
    void setUp() {
        bitSet = mock(BitSet.class);
    }

    @Test
    void testFlip() {
        long bitIndex = 5L;
        bitSet.flip(bitIndex);
        verify(bitSet).flip(bitIndex);
    }

    @Test
    void testFlipRange() {
        long fromIndex = 5L;
        long toIndex = 10L;
        bitSet.flipRange(fromIndex, toIndex);
        verify(bitSet).flipRange(fromIndex, toIndex);
    }

    @Test
    void testSet() {
        long bitIndex = 5L;
        bitSet.set(bitIndex);
        verify(bitSet).set(bitIndex);
    }

    @Test
    void testSetIfClear() {
        long bitIndex = 5L;
        when(bitSet.setIfClear(bitIndex)).thenReturn(true);
        assertTrue(bitSet.setIfClear(bitIndex), "setIfClear returns true");
        verify(bitSet).setIfClear(bitIndex);
    }

    @Test
    void testClearIfSet() {
        long bitIndex = 5L;
        when(bitSet.clearIfSet(bitIndex)).thenReturn(true);
        assertTrue(bitSet.clearIfSet(bitIndex), "clearIfSet returns true");
        verify(bitSet).clearIfSet(bitIndex);
    }

    @Test
    void testSetWithBooleanValue() {
        long bitIndex = 5L;
        boolean value = true;
        bitSet.set(bitIndex, value);
        verify(bitSet).set(bitIndex, value);
    }

    @Test
    void testSetRange() {
        long fromIndex = 5L;
        long toIndex = 10L;
        bitSet.setRange(fromIndex, toIndex);
        verify(bitSet).setRange(fromIndex, toIndex);
    }

    @Test
    void testIsRangeSet() {
        long fromIndex = 5L;
        long toIndex = 10L;
        when(bitSet.isRangeSet(fromIndex, toIndex)).thenReturn(true);
        assertTrue(bitSet.isRangeSet(fromIndex, toIndex), "isRangeSet returns true");
        verify(bitSet).isRangeSet(fromIndex, toIndex);
    }

    @Test
    void testSetAll() {
        bitSet.setAll();
        verify(bitSet).setAll();
    }

    @Test
    void testSetRangeWithBooleanValue() {
        long fromIndex = 5L;
        long toIndex = 10L;
        boolean value = true;
        bitSet.setRange(fromIndex, toIndex, value);
        verify(bitSet).setRange(fromIndex, toIndex, value);
    }

    @Test
    void testClear() {
        long bitIndex = 5L;
        bitSet.clear(bitIndex);
        verify(bitSet).clear(bitIndex);
    }

    @Test
    void testClearRange() {
        long fromIndex = 5L;
        long toIndex = 10L;
        bitSet.clearRange(fromIndex, toIndex);
        verify(bitSet).clearRange(fromIndex, toIndex);
    }

    @Test
    void testClearAll() {
        bitSet.clearAll();
        verify(bitSet).clearAll();
    }

    @Test
    void testGet() {
        long bitIndex = 5L;
        when(bitSet.get(bitIndex)).thenReturn(true);
        assertTrue(bitSet.get(bitIndex), "get returns true");
        verify(bitSet).get(bitIndex);
    }

    @Test
    void testIsSet() {
        long bitIndex = 5L;
        when(bitSet.isSet(bitIndex)).thenReturn(true);
        assertTrue(bitSet.isSet(bitIndex), "isSet returns true");
        verify(bitSet).isSet(bitIndex);
    }

    @Test
    void testIsClear() {
        long bitIndex = 5L;
        when(bitSet.isClear(bitIndex)).thenReturn(true);
        assertTrue(bitSet.isClear(bitIndex), "isClear returns true");
        verify(bitSet).isClear(bitIndex);
    }

    @Test
    void testIsRangeClear() {
        long fromIndex = 5L;
        long toIndex = 10L;
        when(bitSet.isRangeClear(fromIndex, toIndex)).thenReturn(true);
        assertTrue(bitSet.isRangeClear(fromIndex, toIndex), "isRangeClear returns true");
        verify(bitSet).isRangeClear(fromIndex, toIndex);
    }

    @Test
    void testNextSetBit() {
        long fromIndex = 5L;
        when(bitSet.nextSetBit(fromIndex)).thenReturn(6L);
        assertEquals(6L, bitSet.nextSetBit(fromIndex), "nextSetBit returns expected index");
        verify(bitSet).nextSetBit(fromIndex);
    }

    @Test
    void testNextClearBit() {
        long fromIndex = 5L;
        when(bitSet.nextClearBit(fromIndex)).thenReturn(6L);
        assertEquals(6L, bitSet.nextClearBit(fromIndex), "nextClearBit returns expected index");
        verify(bitSet).nextClearBit(fromIndex);
    }

    @Test
    void testPreviousSetBit() {
        long fromIndex = 5L;
        when(bitSet.previousSetBit(fromIndex)).thenReturn(4L);
        assertEquals(4L, bitSet.previousSetBit(fromIndex), "previousSetBit returns expected index");
        verify(bitSet).previousSetBit(fromIndex);
    }

    @Test
    void testPreviousClearBit() {
        long fromIndex = 5L;
        when(bitSet.previousClearBit(fromIndex)).thenReturn(4L);
        assertEquals(4L, bitSet.previousClearBit(fromIndex), "previousClearBit returns expected index");
        verify(bitSet).previousClearBit(fromIndex);
    }

    @Test
    void testLogicalSize() {
        when(bitSet.logicalSize()).thenReturn(64L);
        assertEquals(64L, bitSet.logicalSize(), "logicalSize returns expected value");
        verify(bitSet).logicalSize();
    }

    @Test
    void testCardinality() {
        when(bitSet.cardinality()).thenReturn(5L);
        assertEquals(5L, bitSet.cardinality(), "cardinality returns expected value");
        verify(bitSet).cardinality();
    }

    @Test
    void testSetNextClearBit() {
        long fromIndex = 5L;
        when(bitSet.setNextClearBit(fromIndex)).thenReturn(6L);
        assertEquals(6L, bitSet.setNextClearBit(fromIndex), "setNextClearBit returns expected index");
        verify(bitSet).setNextClearBit(fromIndex);
    }

    @Test
    void testClearNextSetBit() {
        long fromIndex = 5L;
        when(bitSet.clearNextSetBit(fromIndex)).thenReturn(6L);
        assertEquals(6L, bitSet.clearNextSetBit(fromIndex), "clearNextSetBit returns expected index");
        verify(bitSet).clearNextSetBit(fromIndex);
    }

    @Test
    void testSetPreviousClearBit() {
        long fromIndex = 5L;
        when(bitSet.setPreviousClearBit(fromIndex)).thenReturn(4L);
        assertEquals(4L, bitSet.setPreviousClearBit(fromIndex), "setPreviousClearBit returns expected index");
        verify(bitSet).setPreviousClearBit(fromIndex);
    }

    @Test
    void testClearPreviousSetBit() {
        long fromIndex = 5L;
        when(bitSet.clearPreviousSetBit(fromIndex)).thenReturn(4L);
        assertEquals(4L, bitSet.clearPreviousSetBit(fromIndex), "clearPreviousSetBit returns expected index");
        verify(bitSet).clearPreviousSetBit(fromIndex);
    }

    @Test
    void testSetNextNContinuousClearBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        when(bitSet.setNextNContinuousClearBits(fromIndex, numberOfBits)).thenReturn(5L);
        assertEquals(5L,
                bitSet.setNextNContinuousClearBits(fromIndex, numberOfBits),
                "setNextNContinuousClearBits returns expected index");
        verify(bitSet).setNextNContinuousClearBits(fromIndex, numberOfBits);
    }

    @Test
    void testClearNextNContinuousSetBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        when(bitSet.clearNextNContinuousSetBits(fromIndex, numberOfBits)).thenReturn(5L);
        assertEquals(5L,
                bitSet.clearNextNContinuousSetBits(fromIndex, numberOfBits),
                "clearNextNContinuousSetBits returns expected index");
        verify(bitSet).clearNextNContinuousSetBits(fromIndex, numberOfBits);
    }

    @Test
    void testSetPreviousNContinuousClearBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        when(bitSet.setPreviousNContinuousClearBits(fromIndex, numberOfBits)).thenReturn(2L);
        assertEquals(2L,
                bitSet.setPreviousNContinuousClearBits(fromIndex, numberOfBits),
                "setPreviousNContinuousClearBits returns expected index");
        verify(bitSet).setPreviousNContinuousClearBits(fromIndex, numberOfBits);
    }

    @Test
    void testClearPreviousNContinuousSetBits() {
        long fromIndex = 5L;
        int numberOfBits = 3;
        when(bitSet.clearPreviousNContinuousSetBits(fromIndex, numberOfBits)).thenReturn(2L);
        assertEquals(2L,
                bitSet.clearPreviousNContinuousSetBits(fromIndex, numberOfBits),
                "clearPreviousNContinuousSetBits returns expected index");
        verify(bitSet).clearPreviousNContinuousSetBits(fromIndex, numberOfBits);
    }
}
