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
 * Interface representing a read-write lock state with wait registration capabilities.
 * <p>
 * This interface extends {@link ReadWriteLockState}, adding methods to register and
 * deregister waits, and to manage write locks with wait deregistration.
 */
public interface ReadWriteWithWaitsLockState extends ReadWriteLockState {

    /**
     * Registers a wait for the lock.
     */
    void registerWait();

    /**
     * Deregisters a wait for the lock.
     */
    void deregisterWait();

    /**
     * Attempts to acquire a write lock and deregister the wait.
     *
     * @return {@code true} if the write lock was successfully acquired and the wait deregistered,
     *         {@code false} otherwise
     */
    boolean tryWriteLockAndDeregisterWait();

    /**
     * Attempts to upgrade a read lock to a write lock and deregister the wait.
     *
     * @return {@code true} if the lock was successfully upgraded and the wait deregistered,
     *         {@code false} otherwise
     */
    boolean tryUpgradeReadToWriteLockAndDeregisterWait();

    /**
     * Resets the lock state while keeping the wait registrations.
     */
    void resetKeepingWaits();

    /**
     * Returns the locking strategy associated with this lock state.
     *
     * @return the {@link ReadWriteWithWaitsLockingStrategy} used by this lock state
     */
    @Override
    ReadWriteWithWaitsLockingStrategy lockingStrategy();
}
