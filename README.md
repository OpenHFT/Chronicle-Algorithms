# Chronicle-Algorithms

<a href="https://maven-badges.herokuapp.com/maven-central/net.openhft/chronicle-algorithms">
<img src="https://maven-badges.herokuapp.com/maven-central/net.openhft/chronicle-algorithms/badge.svg"/>
</a>
<a href="https://javadoc.io/doc/net.openhft/chronicle-algorithms">
<img src="https://javadoc.io/badge2/net.openhft/chronicle-algorithms/javadoc.svg"/>
</a>

Zero allocation, efficient algorithms for

- hashing
- bit set operations
- access the raw bytes of an data type
- off heap locking

## Quality and static analysis

Chronicle-Algorithms follows the shared Chronicle quality configuration provided by the `java-parent-pom` and `root-parent-pom`.

- Run Checkstyle and SpotBugs as part of the full build with `mvn -P quality clean verify` from the repository root.
- Run module-focused SpotBugs (JDK 11+) with `mvn -P module-quality clean verify` in this module.
