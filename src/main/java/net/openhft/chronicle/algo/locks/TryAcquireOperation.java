/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

/**
 * Callback invoked by {@link AcquisitionStrategy} implementations to attempt lock acquisition.
 *
 * @param <S> the type of the locking strategy
 */
public interface TryAcquireOperation<S extends LockingStrategy> {

    /**
     * Attempts to acquire the lock using the specified locking strategy, access object,
     * target object, and offset.
     *
     * @param strategy the locking strategy to use
     * @param access   the access object to read/write the lock state
     * @param t        the target object on which the lock is to be acquired
     * @param offset   the offset in the target object at which the lock state is located
     * @param <T>      the type of the target object
     * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
     */
    <T> boolean tryAcquire(S strategy, Access<T> access, T t, long offset);
}
