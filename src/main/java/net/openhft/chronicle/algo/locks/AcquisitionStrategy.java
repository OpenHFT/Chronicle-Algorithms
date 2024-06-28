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
 * Interface representing an acquisition strategy for locking mechanisms.
 *
 * @param <S> the type of the locking strategy
 * @param <E> the type of exception that might be thrown during the acquisition process
 */
public interface AcquisitionStrategy<S extends LockingStrategy, E extends Exception> {

    /**
     * Attempts to acquire a lock using the specified locking strategy, access, and operation.
     *
     * @param operation the operation to try acquiring
     * @param strategy  the locking strategy to use
     * @param access    the access mechanism for the resource
     * @param t         the target resource
     * @param offset    the offset within the resource
     * @param <T>       the type of the target resource
     * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
     * @throws E if an exception occurs during the acquisition process
     */
    <T> boolean acquire(
            TryAcquireOperation<? super S> operation, S strategy,
            Access<T> access, T t, long offset) throws E;
}
