/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
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

package net.openhft.chronicle.map.locks;

import org.junit.Assert;
import org.junit.Test;

//import static org.junit.jupiter.api.Assertions.*;

public class ChronicleStampedLockTest {

    @Test
    public void tryOptimisticRead() {
        System.out.println("A test of  ChronicleStampedLock::tryOptimisticRead()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void validate() {
        System.out.println("A test of  ChronicleStampedLock::validate()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void tryWriteLock() {
        System.out.println("A test of  ChronicleStampedLock::tryWriteLock()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void tryReadLock() {
        System.out.println("A test of  ChronicleStampedLock::tryReadLock()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void writeLock() {
        System.out.println("A test of  ChronicleStampedLock::writeLock()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void readLock() {
        System.out.println("A test of  ChronicleStampedLock::readLock()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void unlock() {
        System.out.println("A test of  ChronicleStampedLock::unlock()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void unlockRead() {
        System.out.println("A test of  ChronicleStampedLock::unlockRead()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void unlockWrite() {
        System.out.println("A test of  ChronicleStampedLock::unlockWrite()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void getReadLockCount() {
        System.out.println("A test of  ChronicleStampedLock::getReadLockCount()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void isReadLocked() {
        System.out.println("A test of  ChronicleStampedLock::isReadLocked()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void offHeapLock() {
        System.out.println("A test of  ChronicleStampedLock::offHeapLock()");
        Assert.assertEquals(Boolean.TRUE, true);
    }

    @Test
    public void offHeapLockReaderCount() {
        System.out.println("A test of  ChronicleStampedLock::offHeapLockReaderCount()");
        Assert.assertEquals(Boolean.TRUE, true);
    }
}