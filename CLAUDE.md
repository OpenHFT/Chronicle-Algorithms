# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Chronicle-Algorithms is a Java library providing zero-allocation, high-performance algorithms for:
- Hashing (CityHash, xxHash, MurmurHash3)
- Bit set operations
- Raw byte access for any data type
- Off-heap locking

Part of the Chronicle Software ecosystem, it depends on `chronicle-core` and `chronicle-bytes`.

## Build Commands

```bash
# Build and run tests
mvn clean install

# Run a single test class
mvn test -Dtest=LongHashFunctionTest

# Run a single test method
mvn test -Dtest=LongHashFunctionTest#testHashBytes

# Skip tests
mvn clean install -DskipTests

# Run with specific JaCoCo coverage (thresholds: line 0.56, branch 0.35)
mvn verify
```

## Architecture

### Core Packages

**`net.openhft.chronicle.algo.hashing`** - Hash function implementations
- `LongHashFunction` - Abstract base class for 64-bit hash functions with factory methods (`city_1_1()`, `xx_r39()`, `murmur_3()`)
- Implementations: `CityHash_1_1`, `XxHash_r39`, `MurmurHash_3`
- All hash functions operate on raw memory via `ReadAccess` abstraction

**`net.openhft.chronicle.algo.bytes`** - Memory access abstractions
- `Access<T>` - Combined read/write interface with atomic CAS operations
- `ReadAccess<T>` / `WriteAccess<T>` - Separated read and write interfaces
- `Accessor<S,T,A>` - Transforms source objects into access handles with offset calculations
- Implementations: `NativeAccess` (off-heap), `ByteBufferAccess`, `BytesAccess`

**`net.openhft.chronicle.algo.bitset`** - Bit set operations
- `BitSetFrame` - Core interface for bit manipulation (set, clear, flip, search)
- `BitSetAlgorithm` - Algorithm abstraction (flat vs hierarchical)
- `SingleThreadedFlatBitSetFrame` / `ConcurrentFlatBitSetFrame` - Thread-safe and non-thread-safe implementations

**`net.openhft.chronicle.algo.locks`** - Off-heap locking strategies
- `LockingStrategy` - Base interface for lock operations on raw memory
- `ReadWriteLockingStrategy` - Read/write lock semantics
- `ReadWriteUpdateLockingStrategy` - Adds upgrade capability
- `ReadWriteWithWaitsLockingStrategy` - Adds wait tracking
- All locks operate via `Access<T>` on memory at specified offsets

### Key Design Patterns

1. **Access Abstraction**: All algorithms operate through `Access<T>` interface, enabling operation on native memory, ByteBuffers, or Chronicle Bytes without allocation.

2. **Offset-Based Operations**: Lock and bit set operations take `(Access<T>, T handle, long offset)` parameters, allowing multiple data structures to share the same memory region.

3. **State as Long**: Lock states are encoded as `long` values, enabling atomic operations and persistence.
