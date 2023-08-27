package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.api.InstantEvent;
import codes.nibby.callsign.api.IntervalEndEvent;
import codes.nibby.callsign.api.IntervalStartEvent;
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

    public static final long UNDEFINED_EARLIEST_EVENT_START_TIME_NS = Long.MAX_VALUE;
    public static final long UNDEFINED_LATEST_EVENT_END_TIME_NS = Long.MIN_VALUE;

    protected final Path path;
    protected Connection connection;

    protected Long earliestEventStartTimeNs = UNDEFINED_EARLIEST_EVENT_START_TIME_NS;
    protected Long latestEventEndTimeNs = UNDEFINED_LATEST_EVENT_END_TIME_NS;
    protected boolean hasMetadataRow = false;

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
            loadMetadata();
        } catch (SQLException e) {
            throw new TraceDocumentAccessException("Failed to load document", e);
        }
    }

    private void loadMetadata() throws SQLException {
        assertLoaded();

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + Schema.MetadataTable.TABLE_NAME);

            if (resultSet.next()) {
                earliestEventStartTimeNs = resultSet.getLong(Schema.MetadataTable.COLUMN_EARLIEST_EVENT_START_TIME_NS);
                latestEventEndTimeNs = resultSet.getLong(Schema.MetadataTable.COLUMN_LATEST_EVENT_END_TIME_NS);
                hasMetadataRow = true;
            }
        }
    }

    @Override
    public void unload() throws TraceDocumentAccessException {
        synchronized (stateLock) {
            try {
                connection.close();
                connection = null;

                earliestEventStartTimeNs = UNDEFINED_EARLIEST_EVENT_START_TIME_NS;
                latestEventEndTimeNs = UNDEFINED_LATEST_EVENT_END_TIME_NS;
            } catch (SQLException e) {
                throw new TraceDocumentAccessException("Failed to close SQLite connection", e);
            }
        }
    }

    @Override
    public void streamEntries(List<TraceEntryFilter> filters, Consumer<TraceEvent> consumer) throws TraceDocumentAccessException {
        assertLoaded();

        // TODO: Convert filter settings into SQL condition

        try (Statement statement = connection.createStatement()) {
            // 1. Load attribute headers
            // TODO: Don't need to do this every time if document hasn't changed since it was loaded
            AttributeHeaderData headerData = loadAttributeHeaderData(statement);

            // 2. Stream results
            // Interval events are retrieved differently because of their complementary nature,
            // so do the instant events first because they are the simplest.
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM " + EventsTable.TABLE_NAME + " WHERE " + EventsTable.COLUMN_EVENT_TYPE + " = " + InstantEvent.TYPE
            );

            while (resultSet.next()) {
                processInstantEventEntry(resultSet, headerData, consumer);
            }

            // 2 (cont.) Now stream interval events
            String eventIdColumnName = headerData.getColumnName(Event.SPECIAL_ID_ATTRIBUTE).orElseThrow();

            resultSet = statement.executeQuery(
                "SELECT " +
                    " start." + EventsTable.COLUMN_TIME_NS + ", " +
                    " finish.* " +
                "FROM " + EventsTable.TABLE_NAME + " start " +
                    "JOIN " + EventsTable.TABLE_NAME + " finish " +
                        "ON start." + eventIdColumnName + " = finish." + EventsTable.COLUMN_CORRELATION_ID
            );

            while (resultSet.next()) {
                processIntervalEventEntry(resultSet, headerData, consumer);
            }

        } catch (SQLException e) {
            throw new TraceDocumentAccessException(e);
        }
    }

    private void processIntervalEventEntry(ResultSet resultSet, AttributeHeaderData headerData, Consumer<TraceEvent> consumer) {

    }

    @Override
    public long getEarliestEventStartTimeNs() {
        assertLoaded();
        return earliestEventStartTimeNs;
    }

    @Override
    public long getLatestEventEndTimeNs() {
        assertLoaded();
        return latestEventEndTimeNs;
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

    private void processInstantEventEntry(ResultSet resultSet, AttributeHeaderData headerData, Consumer<TraceEvent> consumer) throws SQLException {
        String type = resultSet.getString(EventsTable.COLUMN_EVENT_TYPE);
        long timeNs = resultSet.getLong(EventsTable.COLUMN_TIME_NS);

        if (!InstantEvent.TYPE.equals(type)) {
            return;
        }

        Map<String, String> attributes = loadEntryAttributes(resultSet, headerData);
        TraceEvent entry = new InstantTraceEvent(attributes, timeNs);

        consumer.accept(entry);
    }

    private Map<String, String> loadEntryAttributes(ResultSet resultSet, AttributeHeaderData headerData) throws SQLException {
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
            static final String COLUMN_CORRELATION_ID = "correlation_id";
            static final String COLUMN_TIME_NS = "time_ns";
        }

        static final class AttributeHeaderTable {
            static final String TABLE_NAME = "attribute_name_lookup";

            static final String COLUMN_ID = "id";
            static final String COLUMN_COLUMN_NAME = "column_name";
            static final String COLUMN_ATTRIBUTE_NAME = "attribute_name";
        }

        static final class MetadataTable {
            static final String TABLE_NAME = "metadata";

            static final String COLUMN_ID = "id";
            static final String COLUMN_EARLIEST_EVENT_START_TIME_NS = "earliest_event_start_time_ns";
            static final String COLUMN_LATEST_EVENT_END_TIME_NS = "latest_event_end_time_ns";
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
