/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MemoryUnitTest {

    @ParameterizedTest(name = "{0} aligns {2} {1}")
    @MethodSource("alignmentPairs")
    void alignProducesExpectedMultiples(MemoryUnit target, MemoryUnit source, long amount) {
        long aligned = target.align(amount, source);
        long alignedBits = MemoryUnit.BITS.convert(aligned, source);
        long unitBits = target.toBits(1);
        assertEquals(0, alignedBits % unitBits, "aligned bits divisible by unit bits");
        if (amount >= 0) {
            assertTrue(aligned >= amount, "positive amounts align upwards");
        } else {
            assertTrue(aligned <= amount, "negative amounts align downwards");
        }
    }

    static Stream<Arguments> alignmentPairs() {
        MemoryUnit[] units = MemoryUnit.values();
        return Stream.of(
                37L,
                1025L,
                -37L,
                -8193L
        ).flatMap(amount -> Stream.of(units)
                .flatMap(target -> Stream.of(units)
                        .filter(source -> source.ordinal() < target.ordinal())
                        .map(source -> Arguments.of(target, source, amount))));
    }

    @Test
    @DisplayName("align throws for invalid granularity combinations")
    void alignRejectsFinerOrEqualUnits() {
        for (MemoryUnit unit : MemoryUnit.values()) {
            assertThrows(IllegalStateException.class, () -> unit.align(1, unit), "align rejects equal units");
            for (int coarser = unit.ordinal() + 1; coarser < MemoryUnit.values().length; coarser++) {
                MemoryUnit coarserUnit = MemoryUnit.values()[coarser];
                assertThrows(IllegalStateException.class, () -> unit.align(1, coarserUnit), "align rejects coarser units");
            }
        }
    }

    @Test
    @DisplayName("conversions saturate on overflow")
    void conversionsSaturateAtBounds() {
        assertEquals(Long.MAX_VALUE, MemoryUnit.GIGABYTES.toBits(Long.MAX_VALUE), "toBits saturates positive overflow");
        assertEquals(Long.MIN_VALUE, MemoryUnit.GIGABYTES.toBits(Long.MIN_VALUE), "toBits saturates negative overflow");
        assertEquals(Long.MAX_VALUE, MemoryUnit.MEGABYTES.toBytes(Long.MAX_VALUE), "toBytes saturates positive overflow");
        assertEquals(Long.MIN_VALUE, MemoryUnit.MEGABYTES.toBytes(Long.MIN_VALUE), "toBytes saturates negative overflow");
    }

    @Test
    @DisplayName("align and convert compose correctly")
    void alignAndConvertMatchesSeparateCalls() {
        long amount = 1536; // bytes
        long aligned = MemoryUnit.KILOBYTES.align(amount, MemoryUnit.BYTES);
        assertEquals(2048, aligned, "align rounds bytes to KiB multiple");
        long converted = MemoryUnit.KILOBYTES.convert(aligned, MemoryUnit.BYTES);
        assertEquals(2, converted, "convert aligned bytes to KiB");
        assertEquals(converted, MemoryUnit.KILOBYTES.alignAndConvert(amount, MemoryUnit.BYTES), "alignAndConvert matches align then convert");
    }

    static Stream<Arguments> conversionCases() {
        return Stream.of(
                Arguments.of(MemoryUnit.BITS, 8L, 1L, MemoryUnit.BYTES),
                Arguments.of(MemoryUnit.BYTES, 1L, 1L, MemoryUnit.BYTES),
                Arguments.of(MemoryUnit.CACHE_LINES, 1L, 64L, MemoryUnit.BYTES),
                Arguments.of(MemoryUnit.MEGABYTES, 2L, 2048L, MemoryUnit.KILOBYTES),
                Arguments.of(MemoryUnit.GIGABYTES, 1L, 1024L, MemoryUnit.MEGABYTES)
        );
    }

    @ParameterizedTest(name = "{0}.convert({2} {3}) = {1}")
    @MethodSource("conversionCases")
    void convertBetweenUnits(MemoryUnit target, long expected, long amount, MemoryUnit source) {
        assertEquals(expected, target.convert(amount, source), "convert returns expected value");
    }

    @Test
    @DisplayName("align handles negative values across units")
    void alignSupportsNegativeAmounts() {
        for (MemoryUnit target : MemoryUnit.values()) {
            for (MemoryUnit source : MemoryUnit.values()) {
                if (source.ordinal() < target.ordinal()) {
                    long aligned = target.align(-73, source);
                    long alignedBits = MemoryUnit.BITS.convert(aligned, source);
                    assertEquals(0, alignedBits % target.toBits(1), "aligned bits divisible by unit bits");
                    assertTrue(aligned <= -73, "negative amounts align downwards");
                }
            }
        }
    }
}
