package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public final class WritableSQLiteTimelineDigestDocument extends SQLiteTimelineDigestDocument implements WritableTimelineDigestDocument {

    public WritableSQLiteTimelineDigestDocument(Path path) {
        super(path);
    }

    @Override
    public void loadForWrite() throws IOException {
        try {
            openConnection();
        } catch (SQLException e) {
            throw new IOException("Failed to create database for document: " + path);
        }
    }

    @Override
    public void appendEvent(Event event) {
        synchronized (stateLock) {
            if (connection == null) {
                throw new IllegalStateException("Document not initialized!");
            }
        }
    }
}
