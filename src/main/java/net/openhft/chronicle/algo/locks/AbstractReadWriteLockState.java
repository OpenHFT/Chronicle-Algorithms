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

/**
 * Abstract base class for managing read-write lock state.
 * Implements common behavior for acquiring and releasing write locks.
 */
public abstract class AbstractReadWriteLockState implements ReadWriteLockState {

    /**
     * Attempts to acquire a write lock.
     * This method delegates to {@link #tryWriteLock()}.
     *
     * @return true if the write lock was successfully acquired, false otherwise
     */
    @Override
    public boolean tryLock() {
        return tryWriteLock();
    }

    /**
     * Releases a write lock.
     * This method delegates to {@link #writeUnlock()}.
     */
    @Override
    public void unlock() {
        writeUnlock();
    }
}
