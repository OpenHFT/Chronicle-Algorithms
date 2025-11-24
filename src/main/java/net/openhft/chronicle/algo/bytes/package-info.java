/**
 * Abstractions for zero copy access to bytes like data structures.
 *
 * <p>The types in this package model readable and writable views over a range
 * of backing stores, including on heap arrays, direct {@code ByteBuffer}
 * instances, and Chronicle Bytes. They provide a common interface for hash
 * functions and other algorithms that need uniform, low overhead access to
 * raw bytes.
 *
 * <p>Callers are expected to respect the documented bounds and lifetime of
 * the underlying storage. Implementations are tuned for minimal allocation
 * and are suitable for use in hot paths across Chronicle libraries.
 */
package net.openhft.chronicle.algo.bytes;
