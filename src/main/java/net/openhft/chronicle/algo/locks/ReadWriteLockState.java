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
 * Interface representing the state of a read-write lock.
 * Provides methods for acquiring and releasing read and write locks,
 * as well as upgrading and downgrading lock states.
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
