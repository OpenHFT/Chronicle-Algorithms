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
 * Interface representing the state of a lock.
 * Provides methods for attempting to acquire the lock, releasing the lock, and resetting the lock state.
 */
public interface LockState {

    /**
     * Attempts to acquire the lock.
     *
     * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
     */
    boolean tryLock();

    /**
     * Releases the lock.
     */
    void unlock();

    /**
     * Resets the lock state.
     */
    void reset();

    /**
     * Retrieves the current state of the lock.
     *
     * @return the current state of the lock
     */
    long getState();

    /**
     * Retrieves the locking strategy associated with this lock state.
     *
     * @return the locking strategy
     */
    LockingStrategy lockingStrategy();
}
