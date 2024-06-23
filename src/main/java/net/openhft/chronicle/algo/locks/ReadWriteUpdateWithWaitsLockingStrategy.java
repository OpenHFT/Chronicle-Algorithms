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
 * Interface representing a read-write-update lock with wait registration capabilities.
 * <p>
 * This interface extends both {@link ReadWriteUpdateLockingStrategy} and {@link ReadWriteWithWaitsLockingStrategy},
 * adding the capability to upgrade an update lock to a write lock while deregistering the wait state.
 */
public interface ReadWriteUpdateWithWaitsLockingStrategy
        extends ReadWriteUpdateLockingStrategy, ReadWriteWithWaitsLockingStrategy {

    /**
     * Attempts to upgrade an update lock to a write lock and deregister the wait state.
     *
     * @param access the access strategy for the lock
     * @param t the object containing the lock
     * @param offset the offset of the lock state within the object
     * @param <T> the type of the object containing the lock
     * @return {@code true} if the lock was successfully upgraded and the wait state deregistered,
     *         {@code false} otherwise
     */
    <T> boolean tryUpgradeUpdateToWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset);
}
