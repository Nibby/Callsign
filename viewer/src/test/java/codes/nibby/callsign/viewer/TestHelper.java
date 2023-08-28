package codes.nibby.callsign.viewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestHelper {

    private TestHelper() {

    }

    public static Path createTestDataDirectory() throws IOException {
        return Files.createTempDirectory("CallSignTest");
    }
}
