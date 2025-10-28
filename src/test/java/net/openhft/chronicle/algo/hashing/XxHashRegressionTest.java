package net.openhft.chronicle.algo.hashing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class XxHashRegressionTest {

    private static final long[] XX_HASH_WITHOUT_SEED = loadVector(
            "XxHashTest.java", "HASHES_OF_LOOPING_BYTES_WITHOUT_SEED");
    private static final long[] XX_HASH_WITH_SEED = loadVector(
            "XxHashTest.java", "HASHES_OF_LOOPING_BYTES_WITH_SEED_42");
    private static final int MAX_LEN = Math.min(XX_HASH_WITHOUT_SEED.length, XX_HASH_WITH_SEED.length) - 1;

    static IntStream lengths() {
        return IntStream.rangeClosed(0, MAX_LEN);
    }

    @ParameterizedTest(name = "unseeded len={0}")
    @MethodSource("lengths")
    void unseededVectorsMatchReference(int len) {
        byte[] data = HashTestSupport.loopingBytes(len);
        HashTestSupport.assertHashMatchesVectors(LongHashFunction.xx_r39(),
                data,
                XX_HASH_WITHOUT_SEED[len]);
    }

    @ParameterizedTest(name = "seeded len={0}")
    @MethodSource("lengths")
    void seededVectorsMatchReference(int len) {
        byte[] data = HashTestSupport.loopingBytes(len);
        HashTestSupport.assertHashMatchesVectors(LongHashFunction.xx_r39(42L),
                data,
                XX_HASH_WITH_SEED[len]);
    }

    private static long[] loadVector(String fileName, String arrayName) {
        Path source = Paths.get("..", "Zero-Allocation-Hashing", "src", "test", "java",
                "net", "openhft", "hashing", fileName);
        String content;
        try {
            byte[] bytes = Files.readAllBytes(source);
            content = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read reference vectors from " + source, e);
        }
        Pattern pattern = Pattern.compile(arrayName + "\\s*=\\s*(?:new\\s+long\\s*\\[\\s*\\]\\s*)?\\{([^}]*)\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            throw new IllegalStateException("Array " + arrayName + " not found in " + source);
        }
        String body = matcher.group(1);
        return Stream.of(body.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.endsWith("L") ? s.substring(0, s.length() - 1) : s)
                .mapToLong(Long::parseLong)
                .toArray();
    }
}
