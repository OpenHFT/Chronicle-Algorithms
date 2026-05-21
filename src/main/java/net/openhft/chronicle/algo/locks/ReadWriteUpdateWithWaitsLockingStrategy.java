/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

/**
 * Read/write/update locking strategy that also tracks wait registrations.
 * <p>
 * Adds a helper to upgrade an update lock to a write lock while deregistering an outstanding wait.
 */
public interface ReadWriteUpdateWithWaitsLockingStrategy
        extends ReadWriteUpdateLockingStrategy, ReadWriteWithWaitsLockingStrategy {

    /**
     * Attempts to upgrade an update lock to a write lock and deregister the wait state.
     *
     * @param access the access strategy for the lock
     * @param t      the object containing the lock
     * @param offset the offset of the lock state within the object
     * @param <T>    the type of the object containing the lock
     * @return {@code true} if the lock was successfully upgraded and the wait state deregistered,
     * {@code false} otherwise
     */
    <T> boolean tryUpgradeUpdateToWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset);
}
