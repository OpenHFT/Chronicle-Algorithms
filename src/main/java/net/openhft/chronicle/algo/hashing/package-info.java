/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Non cryptographic hash functions and helpers.
 *
 * <p>This package provides a collection of 64 bit hash implementations such
 * as Murmur3, CityHash, and xxHash, together with convenience utilities for
 * hashing primitive values, byte sequences, and composite keys.
 *
 * <p>The focus is on high throughput, high quality hashing suitable for use
 * in off heap data structures and Chronicle components. Algorithm classes
 * are part of the public API, but their internal constants and helper
 * methods are considered implementation details.
 */
package net.openhft.chronicle.algo.hashing;
