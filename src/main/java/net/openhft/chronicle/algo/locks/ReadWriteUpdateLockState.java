//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.locks;

/**
 * Interface representing the state and operations of a read-write-update lock.
 * <p>
 * A read lock allows multiple concurrent reads.
 * An update lock allows concurrent reads but not multiple update locks.
 * A write lock is exclusive.
 */
public interface ReadWriteUpdateLockState extends ReadWriteLockState {

    /**
     * Attempts to acquire an update lock.
     *
     * @return {@code true} if the update lock was successfully acquired, {@code false} otherwise
     */
    boolean tryUpdateLock();

    /**
     * Attempts to upgrade a read lock to an update lock.
     *
     * @return {@code true} if the lock was successfully upgraded to an update lock, {@code false} otherwise
     */
    boolean tryUpgradeReadToUpdateLock();

    /**
     * Attempts to upgrade an update lock to a write lock.
     *
     * @return {@code true} if the lock was successfully upgraded to a write lock, {@code false} otherwise
     */
    boolean tryUpgradeUpdateToWriteLock();

    /**
     * Releases an update lock.
     */
    void updateUnlock();

    /**
     * Downgrades an update lock to a read lock.
     */
    void downgradeUpdateToReadLock();

    /**
     * Downgrades a write lock to an update lock.
     */
    void downgradeWriteToUpdateLock();

    /**
     * Retrieves the locking strategy associated with this lock state.
     *
     * @return the {@link ReadWriteUpdateLockingStrategy} associated with this lock state
     */
    @Override
    ReadWriteUpdateLockingStrategy lockingStrategy();
}
