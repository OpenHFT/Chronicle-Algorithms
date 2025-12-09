/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * High performance bit set implementations and algorithms.
 *
 * <p>Classes in this package provide single threaded and concurrent bit set
 * abstractions backed by flat arrays of machine words. They are designed for
 * low latency, cache friendly operations in Chronicle components that work
 * with large sparse sets of indices or flags.
 *
 * <p>This package is part of the public Chronicle Algorithms API. The overall
 * bit set model and key operations are intended to remain stable, although
 * concrete implementations and internal layout details may change between
 * releases.
 */
package net.openhft.chronicle.algo.bitset;
