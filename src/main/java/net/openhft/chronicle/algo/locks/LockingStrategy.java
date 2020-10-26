/*
 * Copyright 2014-2020 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.algo.bytes.ReadAccess;

public interface LockingStrategy {

    <T> boolean tryLock(Access<T> access, T t, long offset);

    <T> void unlock(Access<T> access, T t, long offset);

    <T> void reset(Access<T> access, T t, long offset);

    long resetState();

    <T> long getState(ReadAccess<T> access, T t, long offset);

    boolean isLocked(long state);

    int lockCount(long state);

    String toString(long state);

    int sizeInBytes();
}
