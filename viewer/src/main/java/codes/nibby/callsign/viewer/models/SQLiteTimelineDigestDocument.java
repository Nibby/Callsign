package codes.nibby.callsign.viewer.models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteTimelineDigestDocument implements TimelineDigestDocument {

    protected static final String EVENT_DATA_TABLE_NAME = "event_data";
    protected static final String ATTRIBUTE_HEADER_TABLE_NAME = "attribute_name_lookup";

    protected final Path path;
    protected Connection connection;

    protected final Object stateLock = new Object();

    public SQLiteTimelineDigestDocument(Path path) {
        this.path = path;
    }

    @Override
    public void load() throws IOException {
        if (Files.exists(path)) {
            throw new IOException("File does not exist: " + path);
        }

        try {
            openConnection();
        } catch (SQLException e) {
            throw new IOException("Failed to load document", e);
        }
    }

    @Override
    public void unload() {
        synchronized (stateLock) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close SQLite connection", e);
            }
        }
    }

    protected final void openConnection() throws SQLException {
        synchronized (stateLock) {
            if (connection != null) {
                throw new IllegalStateException("Connection already exists");
            }

            String url = "jdbc:sqlite:" + path.toAbsolutePath();

            // Has side effect of creating the database if it does not exist
            connection = DriverManager.getConnection(url);
        }
    }
}
