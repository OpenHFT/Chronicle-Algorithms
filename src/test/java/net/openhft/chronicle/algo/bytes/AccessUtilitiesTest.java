/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.algo.bytes.ByteBufferAccessor.Direct;
import net.openhft.chronicle.algo.bytes.ByteBufferAccessor.Generic;
import net.openhft.chronicle.algo.bytes.ByteBufferAccessor.Heap;
import net.openhft.chronicle.algo.bytes.BytesAccesses.Full;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.RandomDataInput;
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AccessUtilitiesTest {

    private static final int LENGTH = 32;
    private static final Random RANDOM = new Random(7);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void copyAndEquivalentAcrossAccessTypes() {
        ByteBuffer source = ByteBuffer.allocateDirect(LENGTH).order(ByteOrder.nativeOrder());
        ByteBuffer target = ByteBuffer.allocate(LENGTH).order(ByteOrder.nativeOrder());
        fillBuffer(source);

        Access.copy(ByteBufferAccess.INSTANCE, source, 0, ByteBufferAccess.INSTANCE, target, 0, LENGTH);
        assertTrue(Access.equivalent(ByteBufferAccess.INSTANCE, source, 0, ByteBufferAccess.INSTANCE, target, 0, LENGTH),
                "copy ByteBuffer->ByteBuffer preserves bytes");

        BytesStore<?, ?> store = BytesStore.nativeStoreWithFixedCapacity(LENGTH);
        try {
            @SuppressWarnings("rawtypes")
            Access bytesAccess = Access.checkedBytesStoreAccess();
            // raw access types are required because the BytesStore generic is self-referential
            Access.copy(ByteBufferAccess.INSTANCE, source, 0, bytesAccess, store, 0, LENGTH);
            assertTrue(Access.equivalent(bytesAccess, store, 0, ByteBufferAccess.INSTANCE, source, 0, LENGTH),
                    "copy ByteBuffer->BytesStore preserves bytes");

            long index = 8;
            bytesAccess.writeLong(store, index, 0L);
            assertTrue(bytesAccess.compareAndSwapLong(store, index, 0L, 123L), "compareAndSwapLong succeeds");
            assertEquals(123L, bytesAccess.readLong(store, index), "compareAndSwapLong writes updated value");
        } finally {
            store.releaseLast();
        }
    }

    @Test
    void copyHandlesMixedSizes() {
        ByteBuffer source = ByteBuffer.allocate(15).order(ByteOrder.nativeOrder());
        ByteBuffer target = ByteBuffer.allocate(20).order(ByteOrder.nativeOrder());
        fillBuffer(source);

        Access.copy(ByteBufferAccess.INSTANCE, source, 0, ByteBufferAccess.INSTANCE, target, 2, 15);
        assertTrue(Access.equivalent(ByteBufferAccess.INSTANCE, source, 0, ByteBufferAccess.INSTANCE, target, 2, 15),
                "copy with offset preserves bytes");

        // early exit branch (source == target && offsets equal)
        Access.copy(ByteBufferAccess.INSTANCE, target, 0, ByteBufferAccess.INSTANCE, target, 0, 10);
    }

    @Test
    void compareAndSwapUnsupportedOnByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.nativeOrder());
        assertThrows(UnsupportedOperationException.class,
                () -> ByteBufferAccess.INSTANCE.compareAndSwapLong(buffer, 0, 0L, 1L),
                "compareAndSwapLong unsupported for ByteBuffer");
    }

    @Test
    void byteOrderReflectsBufferConfiguration() {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        assertEquals(ByteOrder.BIG_ENDIAN, ByteBufferAccess.INSTANCE.byteOrder(buffer), "big-endian preserved");
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(ByteOrder.LITTLE_ENDIAN, ByteBufferAccess.INSTANCE.byteOrder(buffer), "little-endian preserved");
    }

    private static void fillBuffer(ByteBuffer buffer) {
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put(i, (byte) RANDOM.nextInt(256));
        }
    }

    @Test
    void byteBufferAccessorVariantsExposeOffsetsAndHandles() {
        ByteBuffer direct = ByteBuffer.allocateDirect(8);
        ByteBufferAccessor<?> directAccessor = ByteBufferAccessor.unchecked(direct);
        assertSame(Direct.INSTANCE, directAccessor, "direct accessor type");
        assertSame(NativeAccess.instance(), directAccessor.access(), "direct accessor uses NativeAccess");
        assertNull(((Direct) directAccessor).handle(direct), "direct accessor handle is null");
        long directBase = directAccessor.offset(direct, 0);
        assertEquals(directBase + 7, directAccessor.offset(direct, 7), "direct accessor offset scales by index");

        ByteBuffer heap = ByteBuffer.allocate(16);
        ByteBufferAccessor<?> heapAccessor = ByteBufferAccessor.unchecked(heap);
        assertSame(Heap.INSTANCE, heapAccessor, "heap accessor type");
        assertSame(NativeAccess.instance(), heapAccessor.access(), "heap accessor uses NativeAccess");
        assertSame(heap.array(), ((Heap) heapAccessor).handle(heap), "heap accessor handle is array");
        assertEquals(heapAccessor.offset(heap, 0) + 5, heapAccessor.offset(heap, 5), "heap accessor offset scales by index");

        ByteBufferAccessor<ByteBuffer> generic = ByteBufferAccessor.checked();
        assertSame(Generic.INSTANCE, generic, "generic accessor type");
        assertSame(ByteBufferAccess.INSTANCE, generic.access(), "generic accessor uses ByteBufferAccess");
        assertSame(heap, generic.handle(heap), "generic handle is the ByteBuffer");
        assertEquals(3L, generic.offset(heap, 3), "generic offset is raw index");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void bytesAccessesFullDelegatesToBytesStore() {
        BytesStore<?, ?> store = BytesStore.nativeStoreWithFixedCapacity(32);
        try {
            @SuppressWarnings("rawtypes")
            Full access = Full.INSTANCE;
            assertTrue(access.compareAndSwapInt(store, 0, 0, 42), "compareAndSwapInt succeeds");
            assertEquals(42, store.readInt(0), "compareAndSwapInt writes updated value");
            assertTrue(access.compareAndSwapLong(store, 8, 0L, 123L), "compareAndSwapLong succeeds");
            assertEquals(123L, store.readLong(8), "compareAndSwapLong writes updated value");
            assertEquals(store.byteOrder(), access.byteOrder(store), "byteOrder delegates to BytesStore");
        } finally {
            store.releaseLast();
        }
    }

    @Test
    void randomDataInputAccessReadsPrimitiveValues() {
        BytesStore<?, ?> store = BytesStore.nativeStoreWithFixedCapacity(32);
        try {
            store.writeLong(0, 0x0102030405060708L);
            store.writeInt(16, 0x11223344);
            RandomDataInputAccess<RandomDataInput> access = BytesAccesses.RandomDataInputReadAccessEnum.INSTANCE;
            assertEquals(0x11223344, access.readInt(store, 16), "readInt reads expected value");
            assertEquals(0x0102030405060708L, access.readLong(store, 0), "readLong reads expected value");
            assertEquals(store.byteOrder(), access.byteOrder(store), "byteOrder delegates to BytesStore");
        } finally {
            store.releaseLast();
        }
    }

    @Test
    void arrayAccessorOffsetsScalePerElement() {
        boolean[] bools = new boolean[8];
        Access<boolean[]> boolAccess = ArrayAccessors.Boolean.INSTANCE.access();
        assertSame(NativeAccess.instance(), boolAccess, "boolean array accessor uses NativeAccess");
        long base = ArrayAccessors.Boolean.INSTANCE.offset(bools, 0);
        assertEquals(base + 3, ArrayAccessors.Boolean.INSTANCE.offset(bools, 3), "boolean offsets scale by index");

        byte[] bytes = new byte[8];
        assertEquals(
                5,
                ArrayAccessors.Byte.INSTANCE.offset(bytes, 5) - ArrayAccessors.Byte.INSTANCE.offset(bytes, 0),
                "byte offsets scale by index"
        );
    }

    @Test
    void hotSpotStringAccessorExposesBackingStorage() {
        String sample = "Cafe";
        Object handle = CharSequenceAccessor.stringAccessor.handle(sample);
        if (Jvm.isJava9Plus()) {
            assertInstanceOf(byte[].class, handle, "Java 9+ String uses byte[]");
            byte[] asBytes = (byte[]) handle;
            assertTrue(asBytes.length >= sample.length(), "backing byte[] length >= String length");
        } else {
            assertInstanceOf(char[].class, handle, "Java 8 String uses char[]");
            char[] asChars = (char[]) handle;
            assertEquals(sample.length(), asChars.length, "backing char[] length matches String length");
        }
        long base = CharSequenceAccessor.stringAccessor.offset(sample, 0);
        long second = CharSequenceAccessor.stringAccessor.offset(sample, 1);
        long third = CharSequenceAccessor.stringAccessor.offset(sample, 3);
        assertTrue(second > base, "offset increases with index");
        assertTrue(third > second, "offset increases with index");
    }
}
