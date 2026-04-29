/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algorithms.measures;

/**
 * Created by peter on 21/08/15.
 */
public interface AddressWrapper {
    void setAddress(long address, long length);

    long hash();
}
