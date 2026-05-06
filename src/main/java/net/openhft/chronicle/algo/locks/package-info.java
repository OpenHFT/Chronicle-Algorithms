/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Locking strategies and state machines for low latency code.
 *
 * <p>Classes in this package model read write and update locks together with
 * the associated acquisition strategies and lock states. They are intended
 * for use by Chronicle data structures that need fine grained control over
 * contention behaviour without depending directly on {@code java.util.concurrent}
 * locks.
 *
 * <p>The abstractions here describe lock semantics and usage patterns rather
 * than specific synchronisation primitives. They are part of the public
 * Chronicle Algorithms API, but callers should not rely on the internal
 * representation of lock states.
 */
package net.openhft.chronicle.algo.locks;
