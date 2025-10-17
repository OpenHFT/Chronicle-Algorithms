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
 * Represents an operation that attempts to acquire a lock using a given locking strategy.
 *
 * @param <S> the type of the locking strategy
 */
public interface TryAcquireOperation<S extends LockingStrategy> {

    /**
     * Attempts to acquire the lock using the specified locking strategy, access object,
     * target object, and offset.
     *
     * @param strategy the locking strategy to use
     * @param access   the access object to read/write the lock state
     * @param t        the target object on which the lock is to be acquired
     * @param offset   the offset in the target object at which the lock state is located
     * @param <T>      the type of the target object
     * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
     */
    <T> boolean tryAcquire(S strategy, Access<T> access, T t, long offset);
}
