/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

/**
 * Pluggable strategy for acquiring locks using a {@link LockingStrategy} and {@link Access}.
 *
 * @param <S> the type of the locking strategy
 * @param <E> the type of exception that might be thrown during the acquisition process
 */
public interface AcquisitionStrategy<S extends LockingStrategy, E extends Exception> {

    /**
     * Attempts to acquire a lock using the specified locking strategy, access, and operation.
     *
     * @param operation the operation to try acquiring
     * @param strategy  the locking strategy to use
     * @param access    the access mechanism for the resource
     * @param t         the target resource
     * @param offset    the offset within the resource
     * @param <T>       the type of the target resource
     * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
     * @throws E if an exception occurs during the acquisition process
     */
    <T> boolean acquire(
            TryAcquireOperation<? super S> operation, S strategy,
            Access<T> access, T t, long offset) throws E;
}
