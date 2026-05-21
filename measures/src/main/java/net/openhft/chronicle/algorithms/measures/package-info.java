/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Hash function quality measures and related tooling.
 *
 * <p>This package contains small utilities and command line tools used to
 * evaluate the statistical properties of hash functions, for example
 * avalanche behaviour and bit flip bias. It is primarily intended for
 * benchmarking and research, not for use on application hot paths.
 *
 * <p>The contents of this package are considered internal test support for
 * Chronicle Algorithms and may change without notice. Applications should
 * depend on the hashing and bytes abstractions in
 * {@code net.openhft.chronicle.algo.hashing} and
 * {@code net.openhft.chronicle.algo.bytes} instead.
 */
package net.openhft.chronicle.algorithms.measures;
