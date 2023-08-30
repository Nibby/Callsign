package codes.nibby.callsign.viewer.models;

import java.io.IOException;
import java.nio.file.Path;

public class WritableSQLiteDocumentTest extends WritableTraceDocumentTest {

    public WritableSQLiteDocumentTest() throws IOException {
    }

    @Override
    protected WritableSQLiteTraceDocument createWritableInstance(Path testDir) throws IOException {
        var document = new WritableSQLiteTraceDocument(createTestOutputFile(testDir));
        document.initialize();

        return document;
    }

    @Override
    protected TraceDocument createInstance(Path traceDigestFile) throws TraceDocumentAccessException {
        var document = new SQLiteTraceDocument(traceDigestFile);
        document.load();

        return document;
    }

    private static Path createTestOutputFile(Path testDir) {
        return testDir.resolve("testWritableSQLiteTraceDocument");
    }
}
