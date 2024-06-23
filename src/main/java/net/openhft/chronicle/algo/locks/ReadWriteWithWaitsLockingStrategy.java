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
 * Interface representing a read-write lock strategy with wait registration capabilities.
 * <p>
 * This interface extends {@link ReadWriteLockingStrategy}, adding methods to register and
 * deregister waits, and to manage write locks with wait deregistration.
 */
public interface ReadWriteWithWaitsLockingStrategy extends ReadWriteLockingStrategy {

    /**
     * Registers a wait at the specified offset.
     *
     * @param access the access object used to manipulate the lock state
     * @param t the target object
     * @param offset the offset at which to register the wait
     * @param <T> the type of the target object
     */
    <T> void registerWait(Access<T> access, T t, long offset);

    /**
     * Deregisters a wait at the specified offset.
     *
     * @param access the access object used to manipulate the lock state
     * @param t the target object
     * @param offset the offset at which to deregister the wait
     * @param <T> the type of the target object
     */
    <T> void deregisterWait(Access<T> access, T t, long offset);

    /**
     * Attempts to acquire a write lock and deregister the wait at the specified offset.
     *
     * @param access the access object used to manipulate the lock state
     * @param t the target object
     * @param offset the offset at which to attempt the write lock acquisition and wait deregistration
     * @param <T> the type of the target object
     * @return {@code true} if the write lock was successfully acquired and the wait deregistered,
     *         {@code false} otherwise
     */
    <T> boolean tryWriteLockAndDeregisterWait(Access<T> access, T t, long offset);

    /**
     * Attempts to upgrade a read lock to a write lock and deregister the wait at the specified offset.
     *
     * @param access the access object used to manipulate the lock state
     * @param t the target object
     * @param offset the offset at which to attempt the lock upgrade and wait deregistration
     * @param <T> the type of the target object
     * @return {@code true} if the lock was successfully upgraded and the wait deregistered,
     *         {@code false} otherwise
     */
    <T> boolean tryUpgradeReadToWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset);

    /**
     * Resets the lock state while keeping the wait registrations.
     *
     * @param access the access object used to manipulate the lock state
     * @param t the target object
     * @param offset the offset at which to reset the lock state
     * @param <T> the type of the target object
     */
    <T> void resetKeepingWaits(Access<T> access, T t, long offset);

    /**
     * Returns the count of registered waits in the given state.
     *
     * @param state the lock state
     * @return the count of registered waits
     */
    int waitCount(long state);
}
