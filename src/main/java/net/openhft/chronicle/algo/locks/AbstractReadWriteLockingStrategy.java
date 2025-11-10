//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

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
 * Abstract base class for read-write locking strategies.
 * Implements common behavior for acquiring and releasing write locks.
 */
public abstract class AbstractReadWriteLockingStrategy implements ReadWriteLockingStrategy {

    /**
     * Attempts to acquire a write lock.
     * This method delegates to {@link #tryWriteLock(Access, Object, long)}.
     *
     * @param access The Access instance for memory operations
     * @param t      The object to lock
     * @param offset The offset within the object
     * @param <T>    The type of the object
     * @return true if the write lock was successfully acquired, false otherwise
     */
    @Override
    public <T> boolean tryLock(Access<T> access, T t, long offset) {
        return tryWriteLock(access, t, offset);
    }

    /**
     * Releases a write lock.
     * This method delegates to {@link #writeUnlock(Access, Object, long)}.
     *
     * @param access The Access instance for memory operations
     * @param t      The object to unlock
     * @param offset The offset within the object
     * @param <T>    The type of the object
     */
    @Override
    public <T> void unlock(Access<T> access, T t, long offset) {
        writeUnlock(access, t, offset);
    }

    /**
     * Checks if the given state indicates that the lock is read-locked.
     * This method delegates to {@link #readLockCount(long)} and checks if the count is greater than zero.
     *
     * @param state The current lock state
     * @return true if the lock is read-locked, false otherwise
     */
    @Override
    public boolean isReadLocked(long state) {
        return readLockCount(state) > 0;
    }
}
