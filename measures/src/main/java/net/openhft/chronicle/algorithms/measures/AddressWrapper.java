/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algorithms.measures;

/**
 * Minimal adapter that exposes a contiguous block of memory so hashing strategies can be compared
 * without caring about the backing implementation.
 * <p>
 * Implementations typically wrap off-heap memory and feed it into a hash function that operates on
 * raw addresses instead of Java arrays.
 */
public interface AddressWrapper {
    /**
     * Point the wrapper at the supplied address range.
     *
     * @param address start address of the readable data
     * @param length  number of readable bytes from that address
     */
    void setAddress(long address, long length);

    /**
     * Hash the currently configured address range.
     *
     * @return hash value calculated over the configured memory region
     */
    long hash();
}
