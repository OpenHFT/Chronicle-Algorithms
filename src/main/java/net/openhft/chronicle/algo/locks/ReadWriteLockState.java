/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

/**
 * Abstraction over the mutable state of a read-write lock.
 * <p>
 * Exposes operations to acquire/release read and write ownership, perform upgrades/downgrades and
 * exposes the associated {@link ReadWriteLockingStrategy}.
 */
public interface ReadWriteLockState extends LockState {

    /**
     * Attempts to acquire a read lock.
     *
     * @return {@code true} if the read lock was successfully acquired, {@code false} otherwise
     */
    boolean tryReadLock();

    /**
     * Attempts to acquire a write lock.
     *
     * @return {@code true} if the write lock was successfully acquired, {@code false} otherwise
     */
    boolean tryWriteLock();

    /**
     * Attempts to upgrade a read lock to a write lock.
     *
     * @return {@code true} if the lock was successfully upgraded to a write lock, {@code false} otherwise
     */
    boolean tryUpgradeReadToWriteLock();

    /**
     * Releases a read lock.
     */
    void readUnlock();

    /**
     * Releases a write lock.
     */
    void writeUnlock();

    /**
     * Downgrades a write lock to a read lock.
     */
    void downgradeWriteToReadLock();

    /**
     * Returns the locking strategy associated with this read-write lock state.
     *
     * @return the read-write locking strategy
     */
    @Override
    ReadWriteLockingStrategy lockingStrategy();
}
