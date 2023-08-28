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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static codes.nibby.callsign.viewer.models.SQLiteTraceDocument.Schema.AttributeHeaderTable;
import static codes.nibby.callsign.viewer.models.SQLiteTraceDocument.Schema.EventsTable;

public class SQLiteTraceDocument implements TraceDocument {

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
    public void streamTraces(List<TraceEntryFilter> filters, Consumer<Trace> consumer) throws TraceDocumentAccessException {
        assertLoaded();

        try (Statement statement = connection.createStatement()) {
            // TODO: Don't need to do this every time if document hasn't changed since it was loaded
            AttributeHeaderData headerData = loadAttributeHeaderData(statement);

            streamInstantTraces(statement, headerData, filters, consumer);
            streamIntervalTraces(statement, headerData, filters, consumer);
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

    private void streamInstantTraces(
        Statement statement,
        AttributeHeaderData headerData,
        List<TraceEntryFilter> filters,
        Consumer<Trace> consumer
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

    private void processInstantEventEntry(
        ResultSet resultSet,
        AttributeHeaderData headerData,
        Consumer<Trace> consumer
    ) throws SQLException {
        long timeNs = resultSet.getLong(EventsTable.COLUMN_TIME_NS);

        Map<String, String> attributes = loadEntryAttributes(resultSet, headerData);
        Trace entry = new InstantTrace(attributes, timeNs);

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

    /*
        Continuously reading (and incrementing) nextIndex until all columns from headerData have been read.

        Has side effect of incrementing nextIndex to the next unread value once this method completes.
     */
    private Map<String, String> loadEntryAttributesByColumnIndexScan(
        AtomicInteger nextIndex,
        ResultSet resultSet,
        AttributeHeaderData headerData
    ) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();

        Map<String, String> results = new HashMap<>();

        for (int count = 0; count < headerData.columnNameToAttributeName.size(); count++) {
            int readIndex = nextIndex.getAndIncrement();
            String columnName = metadata.getColumnName(readIndex);
            Optional<String> attributeName = headerData.getAttributeName(columnName);

            if (attributeName.isEmpty()) {
                throw new IllegalStateException("Column (index=" + readIndex + ") is not valid attribute data");
            }

            String attributeValue = resultSet.getString(readIndex);
            results.put(attributeName.get(), attributeValue);
        }

        return results;
    }

    private void streamIntervalTraces(
        Statement statement,
        AttributeHeaderData headerData,
        List<TraceEntryFilter> filters,
        Consumer<Trace> consumer
    ) throws SQLException {

        // TODO: Convert filter settings into SQL condition

        final String startEventPrefix = "start";
        final String endEventPrefix = "finish";

        // Actually just a FULL OUTER JOIN, but in SQLite dialect
        // Essentially want to get all interval event pairs that either:
        // - Has start but no end
        // - Has start and has an end
        // - No start but has an end

        ResultSet resultSet = statement.executeQuery(
            "SELECT " +
                " " + startEventPrefix + ".*, " +
                " " + endEventPrefix + ".* " +
            "FROM " + EventsTable.TABLE_NAME + " AS " + startEventPrefix + " " +
                "LEFT OUTER JOIN " + EventsTable.TABLE_NAME + " AS " + endEventPrefix + " " +
                    "ON " + startEventPrefix + "." + EventsTable.COLUMN_EVENT_ID + " = " + endEventPrefix + "." + EventsTable.COLUMN_CORRELATION_ID + " " +
            "WHERE " + startEventPrefix + "." + EventsTable.COLUMN_EVENT_TYPE + " = '" + IntervalStartEvent.TYPE + "' " +

            "UNION ALL " +

            "SELECT " +
            " " + startEventPrefix + ".*, " +
            " " + endEventPrefix + ".* " +
            "FROM " + EventsTable.TABLE_NAME + " AS  " + endEventPrefix + " " +
                "LEFT OUTER JOIN " + EventsTable.TABLE_NAME + " AS " + startEventPrefix + " " +
                    "ON " + startEventPrefix + "." + EventsTable.COLUMN_EVENT_ID + " = " + endEventPrefix + "." + EventsTable.COLUMN_CORRELATION_ID + " " +
            "WHERE " + endEventPrefix + "." + EventsTable.COLUMN_EVENT_TYPE + " = '" + IntervalEndEvent.TYPE + "' " +
                "AND " + startEventPrefix + "." + EventsTable.COLUMN_ID + " IS NULL"
        );

        while (resultSet.next()) {
            processIntervalEventEntry(resultSet, headerData, consumer);
        }
    }

    private void processIntervalEventEntry(ResultSet resultSet, AttributeHeaderData headerData, Consumer<Trace> consumer) throws SQLException {
        // TODO: Can make life so much easier by defining a custom view that excludes columns we don't care about;
        /*
               1: start.id
               2: start.event_id
               3: start.correlation_id
               4: start.event_type
               5: start.time_ns
             5+n: start.[custom_attributes]   (0 <= x <= n)
           5+n+1: end.id
           5+n+2: end.event_id
           5+n+3: end.correlation_id
           5+n+4: end.event_type
           5+n+5: end.time_ns
           5+n+m: end.[custom_attributes]     (0 <= x <= m)
         */

        int index = 1;

        @Nullable String startEventId = resultSet.getString(index++); // start.id
        index++; // skip start.event_id
        index++; // skip start.correlation_id
        index++; // skip start.event_type

        // Read start.time_ns
        long startTimeNs = resultSet.getLong(index++);

        if (startTimeNs == 0) {
            // The event representing the start has probably been chopped off from the raw trace data
            // so assume the event has started some unknown time ago
            startTimeNs = UNDEFINED_START_TIME_NS;
        }

        // Read all attributes from start.*
        var indexRef = new AtomicInteger(index);
        @Nullable Map<String, String> startEventAttributes;

        if (startEventId != null) {
            startEventAttributes = loadEntryAttributesByColumnIndexScan(indexRef, resultSet, headerData);
        } else {
            indexRef.set(index + headerData.columnNameToAttributeName.size());
            startEventAttributes = null;
        }

        index = indexRef.get(); // at end.id
        @Nullable String endEventId = resultSet.getString(index++); // end.id
        index++; // skip end.event_id
        index++; // skip end.correlation_id
        index++; // skip end.event_type

        // Read end.time_ns
        long endTimeNs = resultSet.getLong(index++);

        if (endTimeNs == 0) {
            // The event representing the completion has not been received according to the raw trace
            // data, so assume the event is still ongoing
            endTimeNs = UNDEFINED_END_TIME_NS;
        }

        indexRef.set(index);
        @Nullable Map<String, String> endEventAttributes;
        if (endEventId != null) {
            endEventAttributes = loadEntryAttributesByColumnIndexScan(indexRef, resultSet, headerData);
        } else {
            indexRef.set(index + headerData.columnNameToAttributeName.size());
            endEventAttributes = null;
        }

        Trace entry = new IntervalTrace(startEventAttributes, endEventAttributes, startTimeNs, endTimeNs);

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
