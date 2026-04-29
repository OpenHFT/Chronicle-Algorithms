/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

/**
 * Interface representing a read-write lock state with wait registration capabilities.
 * <p>
 * This interface extends {@link ReadWriteLockState}, adding methods to register and
 * deregister waits, and to manage write locks with wait deregistration.
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
