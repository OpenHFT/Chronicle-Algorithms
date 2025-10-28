package net.openhft.chronicle.algo.hashing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class ReferenceData {

    private static final String RESOURCE_PREFIX = "net/openhft/chronicle/algo/hashing/reference/";

    private ReferenceData() {
    }

    static String load(String fileName) {
        Path source = Paths.get("..", "Zero-Allocation-Hashing", "src", "test", "java",
                "net", "openhft", "hashing", fileName);

        IOException fileReadException = null;
        if (Files.isRegularFile(source)) {
            try {
                byte[] bytes = Files.readAllBytes(source);
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                fileReadException = e;
            }
        }

        String resourcePath = RESOURCE_PREFIX + fileName;
        try (InputStream stream = ReferenceData.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return readStream(stream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read reference vectors from classpath resource " + resourcePath, e);
        }

        if (fileReadException != null) {
            throw new IllegalStateException("Unable to read reference vectors from " + source, fileReadException);
        }

        throw new IllegalStateException("Reference vector file " + fileName
                + " not found. Expected either " + source + " or classpath resource " + resourcePath);
    }

    private static String readStream(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int read;
        while ((read = stream.read(data)) != -1) {
            buffer.write(data, 0, read);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
