/*
 * Copyright 2014-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.algo.test;

import static net.openhft.chronicle.algo.test.AssertWithContext.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.util.function.Supplier;

/**
 * Assertions that automatically attach a context message (class#method) so PMD's
 * UnitTestAssertionsShouldIncludeMessage rule is satisfied without having to
 * hand-write messages on every call.
 */
public final class AssertWithContext {

    private AssertWithContext() {
    }

    private static String context() {
        final String helper = AssertWithContext.class.getName();
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            final String cls = ste.getClassName();
            if (!cls.equals(helper) && !cls.equals(Thread.class.getName())) {
                return cls + "#" + ste.getMethodName();
            }
        }
        return "test";
    }

    private static Supplier<String> contextSupplier() {
        return AssertWithContext::context;
    }

    public static void assertTrue(boolean condition) {
        Assertions.assertTrue(condition, contextSupplier());
    }

    public static void assertFalse(boolean condition) {
        Assertions.assertFalse(condition, contextSupplier());
    }

    public static void assertNull(Object actual) {
        Assertions.assertNull(actual, contextSupplier());
    }

    public static void assertNotNull(Object actual) {
        Assertions.assertNotNull(actual, contextSupplier());
    }

    public static void assertSame(Object expected, Object actual) {
        Assertions.assertSame(expected, actual, contextSupplier());
    }

    public static void assertEquals(Object expected, Object actual) {
        Assertions.assertEquals(expected, actual, contextSupplier());
    }

    public static void assertEquals(long expected, long actual) {
        Assertions.assertEquals(expected, actual, contextSupplier());
    }

    public static void assertEquals(int expected, int actual) {
        Assertions.assertEquals(expected, actual, contextSupplier());
    }

    public static void assertEquals(double expected, double actual, double delta) {
        Assertions.assertEquals(expected, actual, delta, context());
    }

    public static void assertNotEquals(Object unexpected, Object actual) {
        Assertions.assertNotEquals(unexpected, actual, contextSupplier());
    }

    public static void assertNotEquals(long unexpected, long actual) {
        Assertions.assertNotEquals(unexpected, actual, contextSupplier());
    }

    public static void assertNotEquals(int unexpected, int actual) {
        Assertions.assertNotEquals(unexpected, actual, contextSupplier());
    }

    public static void assertThrows(Class<? extends Throwable> expectedType, Executable executable) {
        Assertions.assertThrows(expectedType, executable, contextSupplier());
    }

    public static void assertDoesNotThrow(Executable executable) {
        Assertions.assertDoesNotThrow(executable, contextSupplier());
    }

    public static void assertArrayEquals(byte[] expected, byte[] actual) {
        Assertions.assertArrayEquals(expected, actual, context());
    }

    public static void assertArrayEquals(Object[] expected, Object[] actual) {
        Assertions.assertArrayEquals(expected, actual, context());
    }
}
