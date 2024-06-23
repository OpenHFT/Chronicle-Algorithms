/*
 * Copyright 2014-2020 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
     * @param t the instance to lock
     * @param offset the offset for the lock state
     * @return {@code true} if the update lock was successfully acquired, {@code false} otherwise
     */
    <T> boolean tryUpdateLock(Access<T> access, T t, long offset);

    /**
     * Attempts to upgrade a read lock to an update lock.
     *
     * @param access the access object
     * @param t the instance to upgrade
     * @param offset the offset for the lock state
     * @return {@code true} if the lock was successfully upgraded to an update lock, {@code false} otherwise
     */
    <T> boolean tryUpgradeReadToUpdateLock(Access<T> access, T t, long offset);

    /**
     * Attempts to upgrade an update lock to a write lock.
     *
     * @param access the access object
     * @param t the instance to upgrade
     * @param offset the offset for the lock state
     * @return {@code true} if the lock was successfully upgraded to a write lock, {@code false} otherwise
     */
    <T> boolean tryUpgradeUpdateToWriteLock(Access<T> access, T t, long offset);

    /**
     * Releases an update lock.
     *
     * @param access the access object
     * @param t the instance to unlock
     * @param offset the offset for the lock state
     */
    <T> void updateUnlock(Access<T> access, T t, long offset);

    /**
     * Downgrades an update lock to a read lock.
     *
     * @param access the access object
     * @param t the instance to downgrade
     * @param offset the offset for the lock state
     */
    <T> void downgradeUpdateToReadLock(Access<T> access, T t, long offset);

    /**
     * Downgrades a write lock to an update lock.
     *
     * @param access the access object
     * @param t the instance to downgrade
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
