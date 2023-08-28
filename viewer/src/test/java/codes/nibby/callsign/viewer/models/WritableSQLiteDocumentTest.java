package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.viewer.TestHelper;

import java.io.IOException;
import java.nio.file.Path;

public class WritableSQLiteDocumentTest extends WritableTraceDocumentTest<WritableSQLiteTraceDocument> {

    @Override
    protected WritableSQLiteTraceDocument createInstance() {
        try {
            return new WritableSQLiteTraceDocument(createTestOutputFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path createTestOutputFile() throws IOException {
        return TestHelper.createTestDataDirectory().resolve("testWritableSQLiteTraceDocument");
    }
}
