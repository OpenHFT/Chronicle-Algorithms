/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

/**
 * Read/write lock state that also tracks waiting threads for coordination purposes.
 * <p>
 * Adds wait registration, wait-aware acquisition helpers, and a specialised strategy link.
 */
public interface ReadWriteWithWaitsLockState extends ReadWriteLockState {

    /**
     * Registers a wait for the lock.
     */
    void registerWait();

    /**
     * Deregisters a wait for the lock.
     */
    void deregisterWait();

    /**
     * Attempts to acquire a write lock and deregister the wait.
     *
     * @return {@code true} if the write lock was successfully acquired and the wait deregistered,
     * {@code false} otherwise
     */
    boolean tryWriteLockAndDeregisterWait();

    /**
     * Attempts to upgrade a read lock to a write lock and deregister the wait.
     *
     * @return {@code true} if the lock was successfully upgraded and the wait deregistered,
     * {@code false} otherwise
     */
    boolean tryUpgradeReadToWriteLockAndDeregisterWait();

    /**
     * Resets the lock state while keeping the wait registrations.
     */
    void resetKeepingWaits();

    /**
     * Returns the locking strategy associated with this lock state.
     *
     * @return the {@link ReadWriteWithWaitsLockingStrategy} used by this lock state
     */
    @Override
    ReadWriteWithWaitsLockingStrategy lockingStrategy();
}
