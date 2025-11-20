# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Chronicle-Algorithms is a zero-allocation, high-performance Java library providing efficient algorithms for:
- Hashing (CityHash, MurmurHash, XxHash)
- Bit set operations
- Raw byte access to data types
- Off-heap locking mechanisms

Part of the OpenHFT Chronicle suite, this library focuses on performance-critical operations with minimal memory allocation.

## Build and Test Commands

### Building
```bash
mvn clean install
```

### Running Tests
```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=BitSetTest

# Run a specific test method
mvn test -Dtest=BitSetTest#testFlip
```

### Code Coverage
The project uses JaCoCo for code coverage with targets defined in pom.xml:
- Line coverage: 57.9%
- Branch coverage: 36.4%

## Architecture

### Package Structure

The codebase is organized into four main functional areas under `net.openhft.chronicle.algo`:

#### 1. Hashing (`algo.hashing`)
- **LongHashFunction**: Abstract base class for all hash function implementations. Subclasses should implement methods for single primitives, `hashVoid()`, and the core `hash(Object, ReadAccess, long, long)` method. All other methods delegate to these.
- Hash implementations: `CityHash_1_1`, `MurmurHash_3`, `XxHash_r39`
- All hash functions produce cross-platform consistent results (same output on little-endian and big-endian systems), though performance favors little-endian platforms

#### 2. Bytes Access (`algo.bytes`)
- **Access Pattern**: The core abstraction for reading/writing raw bytes from various data sources
  - `ReadAccess<T>`: Read operations on type T
  - `WriteAccess<T>`: Write operations on type T
  - `Access<T>`: Combines both read and write capabilities
  - `AccessCommon<T>`: Base interface with common metadata operations
- **Accessor Pattern**: Converts between source types and Access handles
  - `Accessor<S, T, A>`: Maps source type S to target type T with access strategy A
  - Implementations for arrays, ByteBuffer, BytesStore, CharSequence
- **Key implementations**:
  - `NativeAccess`: Direct native memory access
  - `ByteBufferAccess`: Access ByteBuffer contents
  - `BytesAccesses`: Access Chronicle Bytes BytesStore
  - Array accessors for all primitive types
- The Access/Accessor pattern enables uniform byte-level operations across heterogeneous data sources (heap arrays, direct buffers, off-heap memory)

#### 3. Bit Sets (`algo.bitset`)
- **BitSet**: Main interface with rigid `logicalSize()` (unlike java.util.BitSet which has dynamic capacity)
- **BitSetAlgorithm**: Static utility methods operating on raw memory via Access pattern
- **BitSetFrame**: Combines BitSet interface with Access-based implementation
  - `SingleThreadedFlatBitSetFrame`: Optimized for single-threaded use
  - `ConcurrentFlatBitSetFrame`: Thread-safe implementation with atomic operations
- **ReusableBitSet**: Adapter for reusing BitSet instances with different underlying storage
- All operations throw IndexOutOfBoundsException for out-of-bounds access

#### 4. Locks (`algo.locks`)
- Off-heap locking strategies for concurrent access control
- **LockingStrategy**: Base interface for lock/unlock/reset operations via Access pattern
- **Lock state interfaces**: Separate state from strategy
  - `ReadWriteLockState` / `ReadWriteLockingStrategy`
  - `ReadWriteUpdateLockState` / `ReadWriteUpdateLockingStrategy` (adds update locks)
  - `ReadWriteUpdateWithWaitsLockState` / corresponding strategy (tracks waiting threads)
- **AcquisitionStrategy**: Defines try/lock behaviors (`TryAcquireOperation`, `TryAcquireOperations`)
- Vanilla implementations: `VanillaReadWriteUpdateWithWaitsLockingStrategy`, `VanillaReadWriteWithWaitsLockingStrategy`
- All locking is managed through the Access abstraction, enabling off-heap lock storage

### Key Design Patterns

1. **Access/Accessor Abstraction**: Unified interface for byte-level operations on diverse data sources. Access defines operations on a handle type T, while Accessor converts sources to Access handles with offset calculations.

2. **Zero-Allocation**: Algorithms are designed to avoid object allocation in hot paths. Prefer primitive types, reuse objects (e.g., ReusableBitSet), and operate directly on raw memory.

3. **Off-Heap Support**: All core algorithms work via the Access pattern, allowing operations on off-heap memory without GC pressure.

4. **Strategy Pattern**: Locking and hashing use strategy interfaces to allow algorithm selection and customization.

## Dependencies

Core Chronicle dependencies:
- `chronicle-core`: Foundation utilities
- `chronicle-bytes`: Byte buffer abstractions

Test framework uses both JUnit 4 and JUnit 5 (Jupiter), plus Mockito for mocking.

## License

Apache 2.0 (SPDX-License-Identifier: Apache-2.0)
Copyright 2013-2025 chronicle.software
