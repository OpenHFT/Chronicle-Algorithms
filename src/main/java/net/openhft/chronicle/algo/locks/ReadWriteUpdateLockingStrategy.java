//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

/**
 * Interface defining the logic of read-write-update lock state transitions.
 * <p>
 * A read lock allows multiple concurrent reads.
 * An update lock allows concurrent reads but not multiple update locks.
 * A write lock is exclusive.
 */
public interface ReadWriteUpdateLockingStrategy extends ReadWriteLockingStrategy {

    /**
     * Attempts to acquire an update lock.
     *
     * @param access the access object
     * @param t      the instance to lock
     * @param offset the offset for the lock state
     * @return {@code true} if the update lock was successfully acquired, {@code false} otherwise
     */
    <T> boolean tryUpdateLock(Access<T> access, T t, long offset);

    /**
     * Attempts to upgrade a read lock to an update lock.
     *
     * @param access the access object
     * @param t      the instance to upgrade
     * @param offset the offset for the lock state
     * @return {@code true} if the lock was successfully upgraded to an update lock, {@code false} otherwise
     */
    <T> boolean tryUpgradeReadToUpdateLock(Access<T> access, T t, long offset);

    /**
     * Attempts to upgrade an update lock to a write lock.
     *
     * @param access the access object
     * @param t      the instance to upgrade
     * @param offset the offset for the lock state
     * @return {@code true} if the lock was successfully upgraded to a write lock, {@code false} otherwise
     */
    <T> boolean tryUpgradeUpdateToWriteLock(Access<T> access, T t, long offset);

    /**
     * Releases an update lock.
     *
     * @param access the access object
     * @param t      the instance to unlock
     * @param offset the offset for the lock state
     */
    <T> void updateUnlock(Access<T> access, T t, long offset);

    /**
     * Downgrades an update lock to a read lock.
     *
     * @param access the access object
     * @param t      the instance to downgrade
     * @param offset the offset for the lock state
     */
    <T> void downgradeUpdateToReadLock(Access<T> access, T t, long offset);

    /**
     * Downgrades a write lock to an update lock.
     *
     * @param access the access object
     * @param t      the instance to downgrade
     * @param offset the offset for the lock state
     */
    <T> void downgradeWriteToUpdateLock(Access<T> access, T t, long offset);

    /**
     * Checks if the state indicates that an update lock is held.
     *
     * @param state the state to check
     * @return {@code true} if an update lock is held, {@code false} otherwise
     */
    boolean isUpdateLocked(long state);
}
