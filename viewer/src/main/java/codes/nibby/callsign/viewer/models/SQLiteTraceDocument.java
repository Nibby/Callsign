package codes.nibby.callsign.viewer.models;

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

    public static final long UNDEFINED_START_TIME_NS = Long.MAX_VALUE;
    public static final long UNDEFINED_END_TIME_NS = Long.MIN_VALUE;

    protected final Path path;
    protected Connection connection;

    protected Long earliestEventStartTimeNs = UNDEFINED_START_TIME_NS;
    protected Long latestEventEndTimeNs = UNDEFINED_END_TIME_NS;
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

                earliestEventStartTimeNs = UNDEFINED_START_TIME_NS;
                latestEventEndTimeNs = UNDEFINED_END_TIME_NS;
            } catch (SQLException e) {
                throw new TraceDocumentAccessException("Failed to close SQLite connection", e);
            }
        }
    }

    @Override
    public void streamEntries(List<TraceEntryFilter> filters, Consumer<TraceEvent> consumer) throws TraceDocumentAccessException {
        assertLoaded();

        try (Statement statement = connection.createStatement()) {
            // TODO: Don't need to do this every time if document hasn't changed since it was loaded
            AttributeHeaderData headerData = loadAttributeHeaderData(statement);

            streamInstantEvents(statement, headerData, filters, consumer);
            streamTimedEvents(statement, headerData, filters, consumer);
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

    private void streamInstantEvents(
        Statement statement,
        AttributeHeaderData headerData,
        List<TraceEntryFilter> filters,
        Consumer<TraceEvent> consumer
    ) throws SQLException {

        // TODO: Convert filter settings into SQL condition

        ResultSet resultSet = statement.executeQuery(
            "SELECT * " +
            "FROM " + EventsTable.TABLE_NAME + " " +
            "WHERE " + EventsTable.COLUMN_EVENT_TYPE + " = '" + InstantEvent.TYPE + "'"
        );

        while (resultSet.next()) {
            processInstantEventEntry(resultSet, headerData, consumer);
        }
    }

    private void processInstantEventEntry(ResultSet resultSet, AttributeHeaderData headerData, Consumer<TraceEvent> consumer) throws SQLException {
        long timeNs = resultSet.getLong(EventsTable.COLUMN_TIME_NS);

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

    private void streamTimedEvents(
        Statement statement,
        AttributeHeaderData headerData,
        List<TraceEntryFilter> filters,
        Consumer<TraceEvent> consumer
    ) throws SQLException {

        // TODO: Convert filter settings into SQL condition

        final String startEventPrefix = "start";
        final String endEventPrefix = "finish";

        ResultSet resultSet = statement.executeQuery(
            "SELECT " +
                " " + startEventPrefix + "." + EventsTable.COLUMN_TIME_NS + ", " +
                " " + endEventPrefix + ".* " +
            "FROM " + EventsTable.TABLE_NAME + " " + startEventPrefix + " " +
            "JOIN " + EventsTable.TABLE_NAME + " " + endEventPrefix + " " +
                "ON " + startEventPrefix + "." + EventsTable.COLUMN_EVENT_ID + " = " + endEventPrefix + "." + EventsTable.COLUMN_CORRELATION_ID + " " +
            "WHERE " + startEventPrefix + "." + EventsTable.COLUMN_EVENT_TYPE + " IN  ('" + IntervalStartEvent.TYPE + "', '" + IntervalEndEvent.TYPE + "')"
        );

        while (resultSet.next()) {
            processIntervalEventEntry(resultSet, headerData, consumer);
        }
    }

    private void processIntervalEventEntry(ResultSet resultSet, AttributeHeaderData headerData, Consumer<TraceEvent> consumer) throws SQLException {
        int index = 1;
        long startTimeNs = resultSet.getLong(index++);

        if (startTimeNs == 0) {
            // The event representing the start has probably been chopped off from the raw trace data
            // so assume the event has started some unknown time ago
            startTimeNs = UNDEFINED_START_TIME_NS;
        }

        index++; // skip endEvent.id
        index++; // skip endEvent.event_id
        index++; // skip endEvent.correlation_id

        long endTimeNs = resultSet.getLong(index++);

        if (endTimeNs == 0) {
            // The event representing the completion has not been received according to the raw trace
            // data, so assume the event is still ongoing
            endTimeNs = UNDEFINED_END_TIME_NS;
        }

        Map<String, String> attributes = loadEntryAttributes(resultSet, headerData);

        TraceEvent entry = new TimedTraceEvent(attributes, startTimeNs, endTimeNs);

        consumer.accept(entry);
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

    // TODO: Probably need to version these later
    protected static final class Schema {
        static final class EventsTable {
            static final String TABLE_NAME = "event_data";

            static final String COLUMN_ID = "id";
            static final String COLUMN_EVENT_ID = "event_id";
            static final String COLUMN_CORRELATION_ID = "correlation_id";
            static final String COLUMN_EVENT_TYPE = "event_type";
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
