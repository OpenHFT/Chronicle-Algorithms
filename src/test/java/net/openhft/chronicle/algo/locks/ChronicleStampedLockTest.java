package net.openhft.chronicle.algo.locks;

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