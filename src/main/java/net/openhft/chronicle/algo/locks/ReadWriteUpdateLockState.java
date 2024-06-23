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
