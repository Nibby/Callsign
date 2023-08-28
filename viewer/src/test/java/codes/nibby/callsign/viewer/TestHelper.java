package codes.nibby.callsign.viewer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;

public final class TestHelper {

    private TestHelper() {

    }

    public static Path createTestDataDirectory() throws IOException {
        return Files.createTempDirectory("CallSignTest");
    }

    public static void deleteRecursive(Path dir) throws IOException {
        Files.walkFileTree(
            dir,
            new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            }
        );
    }
}
