# AGENTS.md

## Scope
- Chronicle-Algorithms provides zero-allocation, high-performance algorithms for hashing, bit set operations, raw byte access, and off-heap locking.
- Part of the Chronicle ecosystem with dependencies on chronicle-core and chronicle-bytes.

## Build and test
- Preferred full check:
  - `mkdir -p logs`
  - `mvn verify -l logs/mvn-verify.log`
- Module-scoped example:
  - `mvn -pl <module> -am verify -l logs/mvn-verify.log`
- Test example:
  - `mvn -Dtest=LongHashFunctionTest test -l logs/mvn-test.log`
- Review logs:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log`
- JaCoCo thresholds: line 0.56, branch 0.35.
- Do not commit logs/.

## Repo map
- `net.openhft.chronicle.algo.hashing` includes hash functions such as CityHash, xxHash, and MurmurHash3.
- `net.openhft.chronicle.algo.bytes` covers access abstractions over raw memory, ByteBuffer, and Bytes.
- `net.openhft.chronicle.algo.bitset` provides bit set algorithms and frames.
- `net.openhft.chronicle.algo.locks` handles off-heap locking strategies and state encodings.
- `net.openhft.chronicle.algo.internal` contains internal helpers and low-level utilities.

## Constraints
- Java baseline: 8 (avoid newer language features).
- Source files must stay ISO-8859-1 (code points 0-255). Prefer ASCII; avoid smart quotes and non-breaking spaces.
- Preserve public APIs unless explicitly requested.
- Treat warnings as defects; keep logs clean.
- Avoid extra allocations or synchronisation on hot paths.
- Keep offset-based, access-driven designs when extending algorithms.

## Docs and review checklist
- Keep docs, tests, and code in sync and add tests for new behaviour.
- For large mechanical changes, declare the transformation rule and keep it consistent.
- Add clarifying comments only when intent is non-obvious.

## References
- `OpenHFT/docs/Company-Wide-Tagging.adoc` for tagging and decision record templates.
