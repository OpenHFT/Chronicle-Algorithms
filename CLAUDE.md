# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Repository Overview

Chronicle-Algorithms is a zero-allocation, high-performance Java library providing low-latency primitives for:

* Fast non-cryptographic hashing (Murmur3, CityHash, xxHash) for data structures and indexing
* Cache-friendly bit set implementations for dense and sparse scenarios
* Unified byte accessors for on-heap arrays, direct buffers, Chronicle Bytes, and raw memory without hidden copying
* Lightweight, off-heap-capable lock strategies tuned for Chronicle primitives

It is a foundational Chronicle library that:

* Depends on **Chronicle-Core** and **Chronicle-Bytes**
* Is consumed by higher-level components like **Chronicle-Map** and **Chronicle-Queue**
* Is part of the wider **OpenHFT Chronicle** suite, focusing on performance-critical operations with minimal memory allocation

---

## Build and Test Commands

### Building

```bash
# Full build with tests
mvn clean verify

# Quiet mode (suppress Maven info logs)
mvn -q clean verify

# Build without tests (faster iteration)
mvn -DskipTests clean install

# Simple install (equivalent to full build unless customised)
mvn clean install
```

### Running Tests

```bash
# Run all tests
mvn test

# Run a single test class
mvn -Dtest=LongHashFunctionTest test
mvn test -Dtest=BitSetTest

# Run a specific test method
mvn -Dtest=LongHashFunctionTest#testSpecificMethod test
mvn test -Dtest=BitSetTest#testFlip
```

### Code Coverage

* Code coverage is measured with **JaCoCo**, configured in `pom.xml`.
* Current targets:

    * Line coverage: **57.9%**
    * Branch coverage: **36.4%**

When adding or modifying code, aim to keep coverage at or above these thresholds, and improve them where practical.

---

## Code Architecture

### Package Structure

The codebase is organised under `net.openhft.chronicle.algo` into four main functional areas.

**Public API packages:**

* `net.openhft.chronicle.algo`
  Core utilities (for example, `MemoryUnit`).

* `net.openhft.chronicle.algo.hashing`
  Non-cryptographic hash functions:

    * `LongHashFunction` (abstract base for hash implementations)
    * `MurmurHash_3`
    * `CityHash_1_1`
    * `XxHash_r39`

* `net.openhft.chronicle.algo.bytes`
  Unified byte access abstractions:

    * `Access<T>` / `ReadAccess<T>` / `WriteAccess<T>` / `AccessCommon<T>`
    * Accessors for arrays, `ByteBuffer`, `BytesStore`, `CharSequence`
    * Key implementations: `NativeAccess`, `ByteBufferAccess`, `BytesAccesses`, array accessors

* `net.openhft.chronicle.algo.bitset`
  Bit set implementations:

    * `BitSet` (rigid `logicalSize()` unlike `java.util.BitSet`)
    * `BitSetAlgorithm` (static utilities on raw memory via Access pattern)
    * `BitSetFrame`, `SingleThreadedFlatBitSetFrame`, `ConcurrentFlatBitSetFrame`
    * `ReusableBitSet` for reusing instances over different storage

* `net.openhft.chronicle.algo.locks`
  Lightweight locking strategies:

    * `LockingStrategy` and lock state interfaces
    * `ReadWriteLockState` / `ReadWriteLockingStrategy`
    * `ReadWriteUpdateLockState` / `ReadWriteUpdateLockingStrategy`
    * `ReadWriteUpdateWithWaitsLockState` / corresponding strategy
    * `AcquisitionStrategy`, `TryAcquireOperation`, `TryAcquireOperations`
    * Vanilla implementations such as `VanillaReadWriteUpdateWithWaitsLockingStrategy`, `VanillaReadWriteWithWaitsLockingStrategy`

**Internal/non-public packages:**

* `net.openhft.chronicle.algo.internal`
  Implementation details; not supported for external use and may change without notice.

* `net.openhft.chronicle.algorithms.measures`
  Hash quality evaluation tools and support tooling; not part of the stable public API.

### Key Design Patterns

1. **Access / Accessor Abstraction**

   The bytes package uses a two-level abstraction:

    * `Access<T>` / `ReadAccess<T>` / `WriteAccess<T>` / `AccessCommon<T>`
      Define the primitive operations to read and write bytes (and primitives) for a handle type `T`.

    * `Accessor<S, T, A>`
      Converts a source type `S` (arrays, `ByteBuffer`, `BytesStore`, `CharSequence`) into an `Access` handle `T` using strategy `A`.

   This enables uniform, zero-copy operations over heterogeneous storage (heap arrays, direct buffers, off-heap memory) while keeping algorithms storage-agnostic.

2. **Flyweight Pattern for Bit Sets**

    * `BitSetFrame` and `ReusableBitSet` act as views over underlying storage.
    * Frames overlay contiguous memory (on-heap or off-heap) and provide `set/clear/flip` operations without additional allocation.
    * All operations enforce `IndexOutOfBoundsException` for invalid indices.

3. **Lock State and Strategy Separation**

    * Lock state (`ReadWriteLockState`, `ReadWriteUpdateLockState`, etc.) is separated from acquisition strategy (`LockingStrategy`, `AcquisitionStrategy`).
    * This allows different lock behaviours (with/without waits, with update locks, etc.) to reuse the same state encoding and memory layout.

4. **Zero-Allocation and Off-Heap Focus**

    * Core algorithms are designed to avoid allocations in hot paths.
    * Operations work via the Access pattern on both heap and off-heap memory, minimising GC pressure.
    * Reusable structures (for example, `ReusableBitSet`) and primitive-based APIs are preferred.

5. **Strategy Pattern**

    * Hashing and locking algorithms use strategy interfaces to allow algorithm selection and customisation without changing call sites.
    * This is particularly important for choosing different lock implementations or hash functions in performance-sensitive code.

---

## Dependencies

This module depends on:

* **chronicle-core**
  Low-level utilities (`Jvm`, `OS`, resource management, etc.).

* **chronicle-bytes**
  Off-heap memory access and buffer management.

* **slf4j-api**
  Logging facade (if used, should be minimal in hot paths).

* **JetBrains annotations** (`annotations`)
  For nullability and other code annotations.

Version management is driven by BOMs:

* `chronicle-bom` (for Chronicle artefacts, e.g. `2.27ea-SNAPSHOT`)
* `third-party-bom` (for third-party libraries, e.g. `3.27ea7`)

Test dependencies:

* JUnit 4 and JUnit 5 (Jupiter)
* Mockito
* Guava testlib

---

## Testing

* Frameworks: **JUnit 4** and **JUnit 5**
* Naming convention: test classes end with `*Test.java`.

Typical commands:

```bash
# Run all tests
mvn test

# Run a specific package's tests
mvn -Dtest="net.openhft.chronicle.algo.hashing.*Test" test

# Run a single test class
mvn -Dtest=LongHashFunctionTest test
mvn test -Dtest=BitSetTest

# Run a specific test method
mvn -Dtest=LongHashFunctionTest#testSpecificMethod test
mvn test -Dtest=BitSetTest#testFlip
```

Guidance for changes:

* Prefer deterministic tests for hashing (distribution/avalanche) and bit sets (boundary indices, dense/sparse cases).
* Keep tests compatible with both JUnit 4 and JUnit 5 where applicable.
* Ensure new tests integrate cleanly with existing Maven Surefire configuration and JaCoCo coverage rules.

---

## Documentation

Documentation is maintained in `src/main/docs/` using AsciiDoc:

* `architecture-overview.adoc` – High-level architecture
* `project-requirements.adoc` – Functional/non-functional requirements with Nine-Box tags
* `decision-log.adoc` – Architecture Decision Records (ADRs)
* `testing-strategy.adoc` – Testing approach
* `security-review.adoc` – Security analysis

**Documentation standards:**

* Format: AsciiDoc (`.adoc`)
* Language: British English
* Character set: ISO-8859-1
* Source highlighter: `:source-highlighter: rouge`

When updating code, keep these docs in sync, especially requirements and decision logs.

---

## Code Quality

The repository follows Chronicle ecosystem quality standards:

* Keep **Checkstyle** and **SpotBugs** clean; justify any suppressions inline.
* Avoid allocations in hot paths; maintain zero-allocation behaviour for core algorithms.
* Maintain off-heap bounds checks in all changes involving direct or Bytes-based access.
* Prefer deterministic property-based tests for hashing and bit set operations.
* Respect existing JaCoCo coverage thresholds; if they are temporarily reduced, document and justify the trade-off.

See `AGENTS.md` for module-specific quality guidelines and Nine-Box requirement mappings.

---

## Important Notes

1. **Non-cryptographic Hashes Only**
   Murmur3, CityHash, and xxHash are designed for speed and distribution, not security.
   Do **not** use them for integrity checking, authentication, or confidentiality.

2. **Internal Packages**
   Anything under `net.openhft.chronicle.algo.internal` or `net.openhft.chronicle.algorithms.measures` is **not** public API.
   Do not reference these from external code; treat them as implementation details.

3. **Off-Heap Focus and Safety**
   APIs are designed for zero-allocation operation with off-heap memory.
   Changes must preserve:

    * Memory safety (bounds checking at API boundaries),
    * Clear ownership and lifetime semantics for underlying storage.

4. **Stable API Surface**
   Public interfaces and classes under documented API packages should remain stable within a major version.
   Internal implementations may evolve freely, but public behaviour must remain consistent with requirements and ADRs.

5. **Nine-Box Requirements**
   Code and comments may reference requirement IDs such as `ALGO-FN-010`, `ALGO-NF-P-001`, etc.
   When modifying behaviour:

    * Preserve or update these tags as appropriate.
    * Keep `project-requirements.adoc` and `decision-log.adoc` in sync with code changes.

---

## License

* **License:** Apache 2.0 (SPDX-License-Identifier: Apache-2.0)
* **Copyright:** 2013–2025 chronicle.software
