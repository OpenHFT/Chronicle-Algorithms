# Chronicle-Algorithms

## Project Overview

Chronicle-Algorithms is a Java library providing zero-allocation, efficient algorithms for hashing, bit set operations, raw byte access, and lightweight off-heap-friendly locking. It is designed for high-performance, low-latency applications and is part of the OpenHFT/Chronicle ecosystem.

The project is built with Maven and has dependencies on `chronicle-core` and `chronicle-bytes`. The public APIs are located in the `net.openhft.chronicle.algo` package and its subpackages: `.bitset`, `.bytes`, `.hashing`, and `.locks`.

## Building and Running

To build the project and run all tests, use the following Maven command:

```sh
mvn -q clean verify
```

To build the project without running tests, you can use:

```sh
mvn -pl Chronicle-Algorithms -am -DskipTests install
```

## Development Conventions

*   **Off-heap friendly:** APIs are designed to work with `Chronicle Bytes` and direct buffers, minimizing allocations.
*   **Performance-focused:** The library prioritizes cache-friendly data structures and efficient algorithms.
*   **Stable APIs:** Public interfaces are intended to be stable within a major version.
*   **Clear Separation:** Internal packages (`net.openhft.chronicle.algo.internal`) are not meant for external use.
*   **Testing:** The project uses JUnit and Mockito for testing. It also includes performance and benchmark tests using JMH.
*   **Code Style:** The code follows standard Java conventions with clear Javadoc documentation.
