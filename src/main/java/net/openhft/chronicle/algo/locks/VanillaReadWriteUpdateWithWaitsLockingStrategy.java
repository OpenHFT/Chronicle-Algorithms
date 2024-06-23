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
import net.openhft.chronicle.algo.bytes.ReadAccess;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.ByteOrder.nativeOrder;

/**
 * Vanilla implementation of a read-write-update lock with waits.
 * This class provides the locking mechanism to handle read, write, and update operations with wait strategies.
 */
public final class VanillaReadWriteUpdateWithWaitsLockingStrategy
        extends AbstractReadWriteLockingStrategy
        implements ReadWriteUpdateWithWaitsLockingStrategy {

    // Offsets and shifts for lock words
    static final long COUNT_WORD_OFFSET = 0L; // Offset for count word
    static final long WAIT_WORD_OFFSET = COUNT_WORD_OFFSET + 4L; // Offset for wait word
    static final int COUNT_WORD_SHIFT = nativeOrder() == LITTLE_ENDIAN ? 0 : 32; // Shift for count word
    static final int WAIT_WORD_SHIFT = nativeOrder() == LITTLE_ENDIAN ? 32 : 0; // Shift for wait word
    static final int READ_BITS = 30; // Number of bits for read locks
    static final int MAX_READ = (1 << READ_BITS) - 1; // Maximum number of read locks
    static final int READ_MASK = MAX_READ; // Mask for read locks
    static final int READ_PARTY = 1; // Read party identifier
    static final int UPDATE_PARTY = 1 << READ_BITS; // Update party identifier
    static final int WRITE_LOCKED_COUNT_WORD = UPDATE_PARTY << 1; // Write lock count word
    static final int MAX_WAIT = Integer.MAX_VALUE; // Maximum wait value
    static final int WAIT_PARTY = 1; // Wait party identifier
    private static final long UNSIGNED_INT_MASK = 0xFFFFFFFFL; // Mask for unsigned int
    private static final ReadWriteUpdateWithWaitsLockingStrategy INSTANCE =
            new VanillaReadWriteUpdateWithWaitsLockingStrategy();

    // Private constructor to prevent instantiation
    private VanillaReadWriteUpdateWithWaitsLockingStrategy() {
    }

    /**
     * Returns the singleton instance of this locking strategy.
     *
     * @return The singleton instance of the VanillaReadWriteUpdateWithWaitsLockingStrategy
     */
    public static ReadWriteUpdateWithWaitsLockingStrategy instance() {
        return INSTANCE;
    }

    /**
     * Retrieves the lock word from the given ReadAccess.
     *
     * @param access The ReadAccess instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return The lock word read from the input
     */
    private static <T> long getLockWord(ReadAccess<T> access, T t, long offset) {
        // Reads the lock word from the specified offset
        return access.readVolatileLong(t, offset);
    }

    /**
     * Performs a compare-and-swap operation on the lock word.
     *
     * @param access   The Access instance
     * @param t        The input handle
     * @param offset   The offset within the input
     * @param expected The expected value
     * @param x        The new value
     * @param <T>      The type of the input handle
     * @return True if the operation was successful, false otherwise
     */
    public static <T> boolean casLockWord(
            Access<T> access, T t, long offset, long expected, long x) {
        // Attempts to swap the lock word atomically
        return access.compareAndSwapLong(t, offset, expected, x);
    }

    /**
     * Extracts the count word from the given lock word.
     *
     * @param lockWord The lock word
     * @return The count word
     */
    private static int countWord(long lockWord) {
        // Extracts the count word from the lock word
        return (int) (lockWord >> COUNT_WORD_SHIFT);
    }

    /**
     * Extracts the wait word from the given lock word.
     *
     * @param lockWord The lock word
     * @return The wait word
     */
    private static int waitWord(long lockWord) {
        // Extracts the wait word from the lock word
        return (int) (lockWord >> WAIT_WORD_SHIFT);
    }

    /**
     * Constructs a lock word from the given count and wait words.
     *
     * @param countWord The count word
     * @param waitWord  The wait word
     * @return The constructed lock word
     */
    public static long lockWord(int countWord, int waitWord) {
        // Combines the count and wait words into a single lock word
        return ((((long) countWord) & UNSIGNED_INT_MASK) << COUNT_WORD_SHIFT) |
                ((((long) waitWord) & UNSIGNED_INT_MASK) << WAIT_WORD_SHIFT);
    }

    /**
     * Retrieves the count word from the given Access instance.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return The count word read from the input
     */
    private static <T> int getCountWord(Access<T> access, T t, long offset) {
        // Reads the count word from the specified offset
        return access.readVolatileInt(t, offset + COUNT_WORD_OFFSET);
    }

    /**
     * Performs a compare-and-swap operation on the count word.
     *
     * @param access   The Access instance
     * @param t        The input handle
     * @param offset   The offset within the input
     * @param expected The expected value
     * @param x        The new value
     * @param <T>      The type of the input handle
     * @return True if the operation was successful, false otherwise
     */
    private static <T> boolean casCountWord(
            Access<T> access, T t, long offset, int expected, int x) {
        // Attempts to swap the count word atomically
        return access.compareAndSwapInt(t, offset + COUNT_WORD_OFFSET, expected, x);
    }

    /**
     * Writes the count word to the given Access instance.
     *
     * @param access   The Access instance
     * @param t        The input handle
     * @param offset   The offset within the input
     * @param countWord The count word to write
     * @param <T>      The type of the input handle
     */
    public static <T> void putCountWord(
            Access<T> access, T t, long offset, int countWord) {
        // Writes the count word to the specified offset
        access.writeOrderedInt(t, offset + COUNT_WORD_OFFSET, countWord);
    }

    /**
     * Checks if the given count word represents a write lock.
     *
     * @param countWord The count word
     * @return True if the count word represents a write lock, false otherwise
     */
    private static boolean writeLocked(int countWord) {
        // Checks if the count word is equal to the write lock count word
        return countWord == WRITE_LOCKED_COUNT_WORD;
    }

    /**
     * Checks if the given count word represents an update lock.
     *
     * @param countWord The count word
     * @return True if the count word represents an update lock, false otherwise
     */
    private static boolean updateLocked(int countWord) {
        // Checks if the update party bit is set in the count word
        return (countWord & UPDATE_PARTY) != 0;
    }

    /**
     * Validates that the given count word represents an update lock.
     *
     * @param countWord The count word
     * @throws IllegalMonitorStateException If the count word does not represent an update lock
     */
    private static void checkUpdateLocked(int countWord) {
        // Throws an exception if the count word does not represent an update lock
        if (!updateLocked(countWord))
            throw new IllegalMonitorStateException("Expected update lock");
    }

    /**
     * Extracts the read count from the given count word.
     *
     * @param countWord The count word
     * @return The read count
     */
    private static int readCount(int countWord) {
        // Extracts the read count from the count word
        return countWord & READ_MASK;
    }

    /**
     * Validates that the given count word represents a read lock.
     *
     * @param countWord The count word
     * @throws IllegalMonitorStateException If the count word does not represent a read lock
     */
    private static void checkReadLocked(int countWord) {
        // Throws an exception if the read count is not greater than zero
        if (readCount(countWord) <= 0)
            throw new IllegalMonitorStateException("Expected read lock");
    }

    /**
     * Checks if the read count in the given count word can be incremented.
     *
     * @param countWord The count word
     * @throws IllegalMonitorStateException If the read count has reached the maximum value
     */
    private static void checkReadCountForIncrement(int countWord) {
        // Throws an exception if the read count has reached the maximum value
        if (readCount(countWord) == MAX_READ) {
            throw new IllegalMonitorStateException(
                    "Lock count reached the limit of " + MAX_READ);
        }
    }

    /**
     * Retrieves the wait word from the given Access instance.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return The wait word read from the input
     */
    public static <T> int getWaitWord(Access<T> access, T t, long offset) {
        // Reads the wait word from the specified offset
        return access.readVolatileInt(t, offset + WAIT_WORD_OFFSET);
    }

    /**
     * Performs a compare-and-swap operation on the wait word.
     *
     * @param access   The Access instance
     * @param t        The input handle
     * @param offset   The offset within the input
     * @param expected The expected value
     * @param x        The new value
     * @param <T>      The type of the input handle
     * @return True if the operation was successful, false otherwise
     */
    public static <T> boolean casWaitWord(
            Access<T> access, T t, long offset, int expected, int x) {
        // Attempts to swap the wait word atomically
        return access.compareAndSwapInt(t, offset + WAIT_WORD_OFFSET, expected, x);
    }

    /**
     * Checks if the wait word can be incremented.
     *
     * @param waitWord The wait word
     * @throws IllegalMonitorStateException If the wait word has reached the maximum value
     */
    public static void checkWaitWordForIncrement(int waitWord) {
        // Throws an exception if the wait word has reached the maximum value
        if (waitWord == MAX_WAIT) {
            throw new IllegalMonitorStateException(
                    "Wait count reached the limit of " + MAX_WAIT);
        }
    }

    /**
     * Checks if the wait word can be decremented.
     *
     * @param waitWord The wait word
     * @throws IllegalMonitorStateException If the wait word is zero
     */
    public static void checkWaitWordForDecrement(int waitWord) {
        // Throws an exception if the wait word is zero
        if (waitWord == 0) {
            throw new IllegalMonitorStateException(
                    "Wait count underflowed");
        }
    }

    /**
     * Tries to acquire a write lock and deregister a wait if successful.
     *
     * @param access   The Access instance
     * @param t        The input handle
     * @param offset   The offset within the input
     * @param lockWord The current lock word
     * @param <T>      The type of the input handle
     * @return True if the operation was successful, false otherwise
     */
    public static <T> boolean tryWriteLockAndDeregisterWait0(
            Access<T> access, T t, long offset, long lockWord) {
        int waitWord = waitWord(lockWord); // Extracts the wait word from the lock word
        checkWaitWordForDecrement(waitWord); // Validates the wait word for decrement
        return casLockWord(access, t, offset, lockWord,
                lockWord(WRITE_LOCKED_COUNT_WORD, waitWord - WAIT_PARTY)); // Attempts to acquire the write lock
    }

    /**
     * Checks if the count word represents an exclusive update lock.
     *
     * @param countWord The count word
     * @return True if the count word represents an exclusive update lock, false otherwise
     */
    public static boolean checkExclusiveUpdateLocked(int countWord) {
        checkUpdateLocked(countWord); // Validates the update lock
        return countWord == UPDATE_PARTY; // Checks for exclusive update lock
    }

    /**
     * Ensures the current thread holds the write lock and updates the count word.
     *
     * @param access    The Access instance
     * @param t         The input handle
     * @param offset    The offset within the input
     * @param countWord The new count word
     * @param <T>       The type of the input handle
     * @throws IllegalMonitorStateException If the current thread does not hold the write lock
     */
    private static <T> void checkWriteLockedAndPut(
            Access<T> access, T t, long offset, int countWord) {
        // Attempts to update the count word, throws an exception if unsuccessful
        if (!casCountWord(access, t, offset, WRITE_LOCKED_COUNT_WORD, countWord))
            throw new IllegalMonitorStateException("Expected write lock");
    }

    @Override
    public long resetState() {
        return 0L; // Resets the state
    }

    @Override
    public <T> void reset(Access<T> access, T t, long offset) {
        // Resets the lock state at the specified offset
        access.writeOrderedLong(t, offset, 0L);
    }

    @Override
    public <T> void resetKeepingWaits(Access<T> access, T t, long offset) {
        // Resets the count word while keeping the wait state
        putCountWord(access, t, offset, 0);
    }

    @Override
    public <T> boolean tryReadLock(Access<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset); // Retrieves the lock word
        int countWord = countWord(lockWord); // Extracts the count word
        if (!writeLocked(countWord) && waitWord(lockWord) == 0) {
            checkReadCountForIncrement(countWord); // Validates the read count for increment
            // Attempts to acquire a read lock
            return casCountWord(access, t, offset, countWord, countWord + READ_PARTY);
        }
        return false; // Read lock acquisition failed
    }

    @Override
    public <T> boolean tryUpgradeReadToUpdateLock(Access<T> access, T t, long offset) {
        int countWord = getCountWord(access, t, offset); // Retrieves the count word
        checkReadLocked(countWord); // Validates the read lock
        // Attempts to upgrade read lock to update lock
        return !updateLocked(countWord) &&
                casCountWord(access, t, offset, countWord, countWord - READ_PARTY + UPDATE_PARTY);
    }

    @Override
    public <T> boolean tryUpgradeReadToWriteLock(Access<T> access, T t, long offset) {
        // Attempts to upgrade read lock to write lock directly
        if (casCountWord(access, t, offset, READ_PARTY, WRITE_LOCKED_COUNT_WORD)) {
            return true;
        } else {
            int countWord = getCountWord(access, t, offset); // Retrieves the count word
            checkReadLocked(countWord); // Validates the read lock
            return false; // Write lock upgrade failed
        }
    }

    @Override
    public <T> boolean tryUpgradeReadToWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset); // Retrieves the lock word
        int countWord = countWord(lockWord); // Extracts the count word
        checkReadLocked(countWord); // Validates the read lock
        // Attempts to upgrade read lock to write lock and deregister wait
        return countWord == READ_PARTY &&
                tryWriteLockAndDeregisterWait0(access, t, offset, lockWord);
    }

    /**
     * Attempts to acquire an update lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the update lock was acquired, false otherwise
     */
    @Override
    public <T> boolean tryUpdateLock(Access<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset); // Retrieves the lock word
        int countWord = countWord(lockWord); // Extracts the count word
        if (!updateLocked(countWord) && !writeLocked(countWord) && waitWord(lockWord) == 0) {
            // Attempts to acquire an update lock
            return casCountWord(access, t, offset, countWord, countWord + UPDATE_PARTY);
        }
        return false; // Update lock acquisition failed
    }

    /**
     * Attempts to acquire the write lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the write lock was acquired, false otherwise
     */
    @Override
    public <T> boolean tryWriteLock(Access<T> access, T t, long offset) {
        // Attempts to acquire the write lock
        return casCountWord(access, t, offset, 0, WRITE_LOCKED_COUNT_WORD);
    }

    /**
     * Attempts to acquire the write lock and deregister wait if successful.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the write lock was acquired and wait was deregistered, false otherwise
     */
    @Override
    public <T> boolean tryWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset); // Retrieves the lock word
        int countWord = countWord(lockWord); // Extracts the count word
        // Attempts to acquire the write lock and deregister wait
        return countWord == 0 && tryWriteLockAndDeregisterWait0(access, t, offset, lockWord);
    }

    /**
     * Registers a wait operation.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void registerWait(Access<T> access, T t, long offset) {
        while (true) {
            int waitWord = getWaitWord(access, t, offset); // Retrieves the wait word
            checkWaitWordForIncrement(waitWord); // Validates the wait word for increment
            // Attempts to register the wait
            if (casWaitWord(access, t, offset, waitWord, waitWord + WAIT_PARTY))
                return;
        }
    }

    /**
     * Deregisters a wait operation.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void deregisterWait(Access<T> access, T t, long offset) {
        while (true) {
            int waitWord = getWaitWord(access, t, offset); // Retrieves the wait word
            checkWaitWordForDecrement(waitWord); // Validates the wait word for decrement
            // Attempts to deregister the wait
            if (casWaitWord(access, t, offset, waitWord, waitWord - WAIT_PARTY))
                return;
        }
    }

    /**
     * Attempts to upgrade an update lock to a write lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the upgrade was successful, false otherwise
     */
    @Override
    public <T> boolean tryUpgradeUpdateToWriteLock(Access<T> access, T t, long offset) {
        // Attempts to upgrade update lock to write lock directly
        if (casCountWord(access, t, offset, UPDATE_PARTY, WRITE_LOCKED_COUNT_WORD)) {
            return true;
        } else {
            int countWord = getCountWord(access, t, offset); // Retrieves the count word
            checkUpdateLocked(countWord); // Validates the update lock
            return false; // Write lock upgrade failed
        }
    }

    /**
     * Attempts to upgrade an update lock to a write lock and deregister wait if successful.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the upgrade and wait deregistration were successful, false otherwise
     */
    @Override
    public <T> boolean tryUpgradeUpdateToWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset); // Retrieves the lock word
        int countWord = countWord(lockWord); // Extracts the count word
        // Attempts to upgrade update lock to write lock and deregister wait
        return checkExclusiveUpdateLocked(countWord) &&
                tryWriteLockAndDeregisterWait0(access, t, offset, lockWord);
    }

    /**
     * Releases the read lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void readUnlock(Access<T> access, T t, long offset) {
        while (true) {
            int countWord = getCountWord(access, t, offset); // Retrieves the count word
            checkReadLocked(countWord); // Validates the read lock
            // Attempts to release the read lock
            if (casCountWord(access, t, offset, countWord, countWord - READ_PARTY))
                return;
        }
    }

    /**
     * Releases the update lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void updateUnlock(Access<T> access, T t, long offset) {
        while (true) {
            int countWord = getCountWord(access, t, offset); // Retrieves the count word
            checkUpdateLocked(countWord); // Validates the update lock
            // Attempts to release the update lock
            if (casCountWord(access, t, offset, countWord, countWord - UPDATE_PARTY)) {
                return;
            }
        }
    }

    /**
     * Downgrades an update lock to a read lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void downgradeUpdateToReadLock(Access<T> access, T t, long offset) {
        while (true) {
            int countWord = getCountWord(access, t, offset); // Retrieves the count word
            checkUpdateLocked(countWord); // Validates the update lock
            checkReadCountForIncrement(countWord); // Validates the read count for increment
            // Attempts to downgrade update lock to read lock
            if (casCountWord(access, t, offset, countWord, countWord - UPDATE_PARTY + READ_PARTY)) {
                return;
            }
        }
    }

    /**
     * Releases the write lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void writeUnlock(Access<T> access, T t, long offset) {
        checkWriteLockedAndPut(access, t, offset, 0);
    }

    /**
     * Downgrades the write lock to an update lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void downgradeWriteToUpdateLock(Access<T> access, T t, long offset) {
        checkWriteLockedAndPut(access, t, offset, UPDATE_PARTY);
    }

    /**
     * Checks if the given state is update locked.
     *
     * @param state The state to check
     * @return True if the state is update locked, false otherwise
     */
    @Override
    public boolean isUpdateLocked(long state) {
        // Checks if the state is update locked
        return updateLocked(countWord(state));
    }

    /**
     * Downgrades the write lock to a read lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void downgradeWriteToReadLock(Access<T> access, T t, long offset) {
        // Downgrades the write lock to a read lock
        checkWriteLockedAndPut(access, t, offset, READ_PARTY);
    }

    /**
     * Retrieves the current lock state.
     *
     * @param access The ReadAccess instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return The current lock state
     */
    @Override
    public <T> long getState(ReadAccess<T> access, T t, long offset) {
        // Retrieves the current lock state from the specified offset
        return getLockWord(access, t, offset);
    }

    /**
     * Retrieves the count of read locks from the given state.
     *
     * @param state The state to check
     * @return The number of read locks
     */
    @Override
    public int readLockCount(long state) {
        // Returns the number of read locks from the given state
        return readCount(countWord(state));
    }

    /**
     * Checks if the given state is write locked.
     *
     * @param state The state to check
     * @return True if the state is write locked, false otherwise
     */
    @Override
    public boolean isWriteLocked(long state) {
        // Checks if the state is write locked
        return writeLocked(countWord(state));
    }

    /**
     * Retrieves the count of waits from the given state.
     *
     * @param state The state to check
     * @return The number of waits
     */
    @Override
    public int waitCount(long state) {
        // Returns the number of waits from the given state
        return waitWord(state);
    }

    /**
     * Checks if the given state is locked (either read, update, or write locked).
     *
     * @param state The state to check
     * @return True if the state is locked, false otherwise
     */
    @Override
    public boolean isLocked(long state) {
        // Checks if the state is locked
        return countWord(state) != 0;
    }

    /**
     * Retrieves the total lock count from the given state, including read, update, and write locks.
     *
     * @param state The state to check
     * @return The total lock count
     */
    @Override
    public int lockCount(long state) {
        int countWord = countWord(state); // Extracts the count word
        int lockCount = readCount(countWord); // Counts the number of read locks
        if (lockCount > 0) {
            // Returns the total lock count including update locks
            return lockCount + (updateLocked(countWord) ? 1 : 0);
        } else {
            // Returns 1 if the state is write locked, otherwise 0
            return writeLocked(countWord) ? 1 : 0;
        }
    }

    /**
     * Returns a string representation of the current lock state.
     *
     * @param state The state to represent
     * @return A string representation of the lock state
     */
    @Override
    public String toString(long state) {
        // Constructs a string representation of the lock state
        return "[read locks = " + readLockCount(state) +
                ", update locked = " + isUpdateLocked(state) +
                ", write locked = " + isWriteLocked(state) +
                ", waits = " + waitCount(state) + "]";
    }

    /**
     * Returns the size in bytes of the lock state representation.
     *
     * @return The size in bytes
     */
    @Override
    public int sizeInBytes() {
        // Returns the size in bytes of the lock state representation
        return 8;
    }
}
