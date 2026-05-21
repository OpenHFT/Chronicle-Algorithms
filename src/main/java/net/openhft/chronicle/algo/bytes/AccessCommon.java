/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import java.nio.ByteOrder;

/**
 * Common contract for determining the byte order of a readable/writable handle.
 *
 * @param <T> the type of the handle
 */
@FunctionalInterface
interface AccessCommon<T> {

    /**
     * Returns the byte order of the given handle.
     *
     * @param handle the handle whose byte order is to be determined
     * @return the byte order of the given handle
     */
    ByteOrder byteOrder(T handle);
}
