package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.InstantEvent;
import codes.nibby.callsign.api.TimedEvent;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

import static codes.nibby.callsign.viewer.models.SQLiteTraceDocument.Schema.AttributeHeaderTable;
import static codes.nibby.callsign.viewer.models.SQLiteTraceDocument.Schema.EventsTable;

public class SQLiteTraceDocument implements TraceDocument {

    protected final Path path;
    protected Connection connection;

    protected final Object stateLock = new Object();

    public SQLiteTraceDocument(Path path) {
        this.path = path;
    }

    @Override
    public void load() throws TraceDocumentAccessException {
        if (!Files.exists(path)) {
            throw new TraceDocumentAccessException("File does not exist: " + path);
        }

        try {
            openConnection();
        } catch (SQLException e) {
            throw new TraceDocumentAccessException("Failed to load document", e);
        }
    }

    @Override
    public void unload() throws TraceDocumentAccessException {
        synchronized (stateLock) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                throw new TraceDocumentAccessException("Failed to close SQLite connection", e);
            }
        }
    }

    @Override
    public void streamEntries(List<TraceEntryFilter> filters, Consumer<TraceEntry> consumer) throws TraceDocumentAccessException {
        assertLoaded();

        // TODO: Convert filter settings into SQL condition

        try (Statement statement = connection.createStatement()) {
            // 1. Load attribute headers
            // TODO: Don't need to do this every time if document hasn't changed since it was loaded
            AttributeHeaderData headerData = loadAttributeHeaderData(statement);

            // 2. Stream results
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + EventsTable.TABLE_NAME);

            while (resultSet.next()) {
                processTraceEntry(resultSet, headerData, consumer);
            }
        } catch (SQLException e) {
            throw new TraceDocumentAccessException(e);
        }
    }

    private AttributeHeaderData loadAttributeHeaderData(Statement statement) throws SQLException {
        var headerData = new AttributeHeaderData();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM " + AttributeHeaderTable.TABLE_NAME);

        while (resultSet.next()) {
            String columnName = resultSet.getString(AttributeHeaderTable.COLUMN_COLUMN_NAME);
            String attributeName = resultSet.getString(AttributeHeaderTable.COLUMN_ATTRIBUTE_NAME);

            headerData.columnNameToAttributeName.put(columnName, attributeName);
        }

        return headerData;
    }

    private void processTraceEntry(ResultSet resultSet, AttributeHeaderData headerData, Consumer<TraceEntry> consumer) throws SQLException {
        String type = resultSet.getString(EventsTable.COLUMN_EVENT_TYPE);
        String name = resultSet.getString(EventsTable.COLUMN_EVENT_NAME);

        long startTimeNs = resultSet.getLong(EventsTable.COLUMN_START_TIME_NS);
        long endTimeNs = resultSet.getLong(EventsTable.COLUMN_END_TIME_NS);

        boolean validEntry = false;

        if (InstantEvent.TYPE.equals(type) || TimedEvent.TYPE.equals(type)) {
            validEntry = true;
        } else if (type != null) {
            throw new IllegalStateException("Unsupported event type: " + type);
        } else {
            // TODO: Handle this better
            System.err.println("Ignored an event with NULL type");
        }

        if (!validEntry) {
            return;
        }

        Map<String, String> attributes = loadTraceEntryAttributes(resultSet, headerData);
        TraceEntry entry;

        if (InstantEvent.TYPE.equals(type)) {
            entry = new InstantTraceEntry(name, attributes, startTimeNs);
        } else if (TimedEvent.TYPE.equals(type)) {
            entry = new TimedTraceEntry(name, attributes,  startTimeNs, endTimeNs);
        } else {
            throw new IllegalStateException();
        }

        consumer.accept(entry);
    }

    private Map<String, String> loadTraceEntryAttributes(ResultSet resultSet, AttributeHeaderData headerData) throws SQLException {
        Map<String, String> attributes = new HashMap<>();

        for (String columnName : headerData.getAllColumnNames()) {
            @Nullable String value = resultSet.getString(columnName);

            if (value == null) {
                continue;
            }

            Optional<String> attributeName = headerData.getAttributeName(columnName);

            if (attributeName.isEmpty()) {
                System.err.println("No attribute name found for column name: " + columnName);
                continue;
            }

            attributes.put(attributeName.get(), value);
        }

        return attributes;
    }

    private void assertLoaded() {
        synchronized (stateLock) {
            if (connection == null) {
                throw new IllegalStateException("Document not loaded");
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

    // TODO: Probably need to version these later
    protected static final class Schema {
        static final class EventsTable {
            static final String TABLE_NAME = "event_data";

            static final String COLUMN_ID = "id";
            static final String COLUMN_EVENT_TYPE = "event_type";
            static final String COLUMN_EVENT_NAME = "event_name";
            static final String COLUMN_START_TIME_NS = "start_time_ns";
            static final String COLUMN_END_TIME_NS = "end_time_ns";
        }

        static final class AttributeHeaderTable {
            static final String TABLE_NAME = "attribute_name_lookup";

            static final String COLUMN_ID = "id";
            static final String COLUMN_COLUMN_NAME = "column_name";
            static final String COLUMN_ATTRIBUTE_NAME = "attribute_name";
        }
    }

    private static final class AttributeHeaderData {

        private final BiMap<String, String> columnNameToAttributeName = HashBiMap.create();

        public Optional<String> getAttributeName(String columnName) {
            @Nullable String attributeName = columnNameToAttributeName.get(columnName);
            return Optional.ofNullable(attributeName);
        }

        public Optional<String> getColumnName(String attributeName) {
            @Nullable String columnName = columnNameToAttributeName.inverse().get(attributeName);
            return Optional.ofNullable(columnName);
        }

        public Set<String> getAllColumnNames() {
            return columnNameToAttributeName.keySet();
        }
    }
}
