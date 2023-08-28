package codes.nibby.callsign.viewer.models;

import java.io.IOException;
import java.nio.file.Path;

public class WritableSQLiteDocumentTest extends WritableTraceDocumentTest<WritableSQLiteTraceDocument> {

    public WritableSQLiteDocumentTest() throws IOException {
    }

    @Override
    protected WritableSQLiteTraceDocument createInstance(Path testDir) {
        try {
            return new WritableSQLiteTraceDocument(createTestOutputFile(testDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path createTestOutputFile(Path testDir) throws IOException {
        return testDir.resolve("testWritableSQLiteTraceDocument");
    }
}
