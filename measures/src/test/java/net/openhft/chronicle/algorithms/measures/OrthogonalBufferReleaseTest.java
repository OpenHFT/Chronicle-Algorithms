/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algorithms.measures;

import net.openhft.chronicle.bytes.NativeBytes;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.BackgroundResourceReleaser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrthogonalBufferReleaseTest {
    @Test
    void completedSamplesReleaseBuffersAndPreserveBitFlipsAndPenalty() {
        NativeBytes<?> warmup = NativeBytes.nativeBytes(8);
        warmup.releaseLast();
        BackgroundResourceReleaser.releasePendingResources();
        long before = OS.memory().nativeMemoryUsed();
        for (boolean lowBits : new boolean[]{false, true}) {
            long[] hashes = new long[64];
            AddressWrapper wrapper = new AddressWrapper() {
                long address;
                public void setAddress(long address, long length) { this.address = address; assertEquals(8, length); }
                public long hash() { return OS.memory().readLong(address); }
            };
            assertTrue(OrtogonalBitsScore.sample(wrapper, lowBits, hashes) >= 0);
            for (int i = 1; i < hashes.length; i++)
                assertEquals(2, Long.bitCount(hashes[i] ^ hashes[i - 1]));
            assertEquals(64L * 63 / 2 * (1L << 15), OrtogonalBitsScore.penalty(hashes));
        }
        BackgroundResourceReleaser.releasePendingResources();
        assertEquals(before, OS.memory().nativeMemoryUsed());
        assertEquals(0, OrtogonalBitsScore.penalty(new long[]{0, -1}));
    }

    @Test
    void rejectedAddressReleasesNativeBuffer() { failsWithoutLeaking(true); }

    @Test
    void failedHashReleasesNativeBuffer() { failsWithoutLeaking(false); }

    private void failsWithoutLeaking(boolean rejectAddress) {
        NativeBytes<?> warmup = NativeBytes.nativeBytes(1024);
        warmup.releaseLast();
        BackgroundResourceReleaser.releasePendingResources();
        long before = OS.memory().nativeMemoryUsed();
        IllegalStateException failure = new IllegalStateException("wrapper failed");
        AddressWrapper wrapper = new AddressWrapper() {
            public void setAddress(long address, long length) {
                if (rejectAddress) throw failure;
            }
            public long hash() { throw failure; }
        };
        assertSame(failure, assertThrows(IllegalStateException.class, () -> OrtogonalBitsScore.score(wrapper)));
        BackgroundResourceReleaser.releasePendingResources();
        assertEquals(before, OS.memory().nativeMemoryUsed(), "the score must release its native sample buffer");
    }
}
