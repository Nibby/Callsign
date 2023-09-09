package codes.nibby.callsign.viewer.models.document;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.api.InstantEvent;
import codes.nibby.callsign.api.IntervalEndEvent;
import codes.nibby.callsign.api.IntervalStartEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static codes.nibby.callsign.viewer.models.document.SQLiteTraceDocument.Schema.*;

public final class WritableSQLiteTraceDocument extends SQLiteTraceDocument implements WritableTraceDocument {

    private final Map<String, String> attributeNameLookup = new ConcurrentHashMap<>();
    private final AtomicInteger nextAttributeNameId = new AtomicInteger(0);

    public WritableSQLiteTraceDocument(Path path) {
        super(path);
    }

    public void initialize() throws IOException {
        boolean modifyNotCreate = Files.exists(this.path);

        try {
            synchronized (stateLock) {
                initializeImpl(modifyNotCreate);
            }
        } catch (SQLException e) {
            throw new IOException("Failed to initialize SQLite database", e);
        }
    }

    private void initializeImpl(boolean modifyNotCreate) throws SQLException {
        openConnection();

        if (!modifyNotCreate) {
            createInitialTablesAndIndices();
        }

        loadAttributeHeaderData();
    }

    private void createInitialTablesAndIndices() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            createEventsTable(statement);
            createAttributeHeaderTable(statement);
            createMetadataTable(statement);
        }
    }

    private void createAttributeHeaderTable(Statement statement) throws SQLException {
        statement.execute(
            "CREATE TABLE " + AttributeHeaderTable.TABLE_NAME +
            "(" +
                AttributeHeaderTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AttributeHeaderTable.COLUMN_COLUMN_NAME + " TEXT NOT NULL," +
                AttributeHeaderTable.COLUMN_ATTRIBUTE_NAME + " TEXT NOT NULL" +
            ")"
        );

        statement.execute(
            "CREATE INDEX index_column_name " +
            "ON " + AttributeHeaderTable.TABLE_NAME + " (" + AttributeHeaderTable.COLUMN_COLUMN_NAME + ")"
        );

        statement.execute(
            "CREATE INDEX index_attribute_name " +
            "ON " + AttributeHeaderTable.TABLE_NAME + " (" + AttributeHeaderTable.COLUMN_ATTRIBUTE_NAME + ")"
        );
    }

    private void createEventsTable(Statement statement) throws SQLException {
        statement.execute(
            "CREATE TABLE " + EventsTable.TABLE_NAME +
            "(" +
                EventsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                EventsTable.COLUMN_EVENT_ID + " TEXT NOT NULL," +
                EventsTable.COLUMN_CORRELATION_ID + " TEXT NULL," +
                EventsTable.COLUMN_EVENT_TYPE + " TEXT NOT NULL," +
                EventsTable.COLUMN_TIME_NS + " INTEGER NULL" +
            ")"
        );

        statement.execute("CREATE INDEX index_event_id ON event_data (" + EventsTable.COLUMN_EVENT_ID + ")");
        statement.execute("CREATE INDEX index_time_ns ON event_data (" + EventsTable.COLUMN_TIME_NS + ")");
        statement.execute("CREATE INDEX correlation_id ON event_data (" + EventsTable.COLUMN_CORRELATION_ID + ")");
    }

    private void createMetadataTable(Statement statement) throws SQLException {
        statement.execute(
            "CREATE TABLE " + MetadataTable.TABLE_NAME +
            "(" +
                MetadataTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MetadataTable.COLUMN_EARLIEST_EVENT_START_TIME_NS + " INTEGER NOT NULL," +
                MetadataTable.COLUMN_LATEST_EVENT_END_TIME_NS + " INTEGER NOT NULL" +
            ")"
        );
    }

    private void loadAttributeHeaderData() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT seq FROM sqlite_sequence WHERE name = ?")) {
            statement.setString(1, AttributeHeaderTable.TABLE_NAME);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                nextAttributeNameId.set(resultSet.getInt("seq"));
            } else {
                nextAttributeNameId.set(0); // Fresh database
            }
        }

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + AttributeHeaderTable.TABLE_NAME);

            while (resultSet.next()) {
                String columnName = resultSet.getString(AttributeHeaderTable.COLUMN_COLUMN_NAME);
                String attributeName = resultSet.getString(AttributeHeaderTable.COLUMN_ATTRIBUTE_NAME);

                attributeNameLookup.put(columnName, attributeName);
            }
        }
    }

    @Override
    public void appendEvent(Event event) throws IOException {
        synchronized (stateLock) {
            if (connection == null) {
                throw new IllegalStateException("Document not initialized!");
            }

            Set<String> missingAttributeNames = event.getAttributeNames().stream()
                .filter(name -> !attributeNameLookup.containsValue(name))
                .collect(Collectors.toSet());

            try {
                // TODO: This can be greatly optimised, do later

                if (!missingAttributeNames.isEmpty()) {
                    attributeHeaderData = null;
                    createMissingAttributeNames(missingAttributeNames);
                }

                appendEventImpl(event);
            } catch (SQLException e) {
                throw new IOException("An error occurred appending event", e);
            }
        }
    }

    private void createMissingAttributeNames(Set<String> missingAttributeNames) throws SQLException {

        Set<String> newColumnNames = new HashSet<>();

        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO " + AttributeHeaderTable.TABLE_NAME + " ("
                + AttributeHeaderTable.COLUMN_COLUMN_NAME + ", "
                + AttributeHeaderTable.COLUMN_ATTRIBUTE_NAME
                + ") VALUES (?, ?)"
        )) {
            for (String attributeName : missingAttributeNames) {
                String columnName = "attribute_" + nextAttributeNameId.get();

                statement.setString(1, columnName);
                statement.setString(2, attributeName);

                statement.execute();
                nextAttributeNameId.incrementAndGet();

                attributeNameLookup.put(columnName, attributeName);
                newColumnNames.add(columnName);
            }
        }

        for (String newColumnName : newColumnNames) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                    "ALTER TABLE " + EventsTable.TABLE_NAME + " ADD COLUMN " + newColumnName + " TEXT NULL"
                );
            }
        }
    }

    private void appendEventImpl(Event event) throws SQLException {
        StringBuilder additionalAttributeColumns = new StringBuilder();
        StringBuilder additionalAttributeValues = new StringBuilder();

        Map<Integer, String> parameterOffsetToAttributeName = new HashMap<>(attributeNameLookup.size());

        int offset = 0;

        for (var entry  : attributeNameLookup.entrySet()) {
            String columnName = entry.getKey();
            String attributeName = entry.getValue();

            additionalAttributeColumns.append(", ").append(columnName);
            additionalAttributeValues.append(", ?");

            parameterOffsetToAttributeName.put(offset, attributeName);

            offset++;
        }

        String query =
            "INSERT INTO " + EventsTable.TABLE_NAME +
            "(" +
                EventsTable.COLUMN_EVENT_ID + ", " +
                EventsTable.COLUMN_CORRELATION_ID + ", " +
                EventsTable.COLUMN_EVENT_TYPE + ", " +
                EventsTable.COLUMN_TIME_NS +
                additionalAttributeColumns +
            ") " +
            "VALUES (?, ?, ?, ?" + additionalAttributeValues + ")";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int index = 1;

            statement.setString(index++, event.getId().toString());
            statement.setString(index++, event.getCorrelationId() != null ? event.getCorrelationId().toString() : null);
            statement.setString(index++, event.getType());

            long timeNs = event.getTimeNs();
            statement.setLong(index++, timeNs);

            if (event instanceof IntervalStartEvent) {
                updateMetadataIfApplicable(timeNs, null);
            } else if (event instanceof IntervalEndEvent) {
                updateMetadataIfApplicable(null, timeNs);
            } else if (event instanceof InstantEvent instantEvent) {
                updateMetadataIfApplicable(instantEvent.getTimeNs(), instantEvent.getTimeNs());
            } else {
                throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
            }

            for (var attributeEntry : parameterOffsetToAttributeName.entrySet()) {
                int columnOffset = attributeEntry.getKey();
                String attributeName = attributeEntry.getValue();

                statement.setString(index + columnOffset, event.getAttribute(attributeName));
            }

            statement.execute();
        } catch (SQLException e) {
            // TODO: Better error logging
            System.err.println("Failed to execute query:\n" + query);
            throw e;
        }
    }

    private void updateMetadataIfApplicable(Long startTimeNs, Long endTimeNs) throws SQLException {
        boolean metadataChanged = false;

        if (startTimeNs != null && !Objects.equals(earliestEventStartTimeNs, startTimeNs)) {
            earliestEventStartTimeNs = earliestEventStartTimeNs == UNDEFINED_START_TIME_NS
                ? startTimeNs
                : Math.min(earliestEventStartTimeNs, startTimeNs);

            metadataChanged = true;
        }

        if (endTimeNs != null && !Objects.equals(latestEventEndTimeNs, endTimeNs)) {
            latestEventEndTimeNs = latestEventEndTimeNs == UNDEFINED_END_TIME_NS
                ? endTimeNs
                : Math.max(latestEventEndTimeNs, endTimeNs);

            metadataChanged = true;
        }

        if (metadataChanged) {
            if (hasMetadataRow) {
                updateMetadataRow();
            } else {
                createMetadataRow();
                hasMetadataRow = true;
            }
        }
    }

    private void createMetadataRow() throws SQLException {
        try (var statement = connection.prepareStatement(
            "INSERT INTO " + MetadataTable.TABLE_NAME +
            " (" +
                MetadataTable.COLUMN_EARLIEST_EVENT_START_TIME_NS + ", " +
                MetadataTable.COLUMN_LATEST_EVENT_END_TIME_NS +
            ") VALUES (?, ?)"
        )) {
            int index = 1;

            statement.setLong(index++, earliestEventStartTimeNs);
            statement.setLong(index++, latestEventEndTimeNs);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated != 1) {
                throw new IllegalStateException("Failed to update metadata row! Rows updated: " + rowsUpdated);
            }
        }
    }

    private void updateMetadataRow() throws SQLException {
        try (var statement = connection.prepareStatement(
            "UPDATE " + MetadataTable.TABLE_NAME + " SET " +
                MetadataTable.COLUMN_EARLIEST_EVENT_START_TIME_NS + " = ?, " +
                MetadataTable.COLUMN_LATEST_EVENT_END_TIME_NS + " = ?" +
            " WHERE 1 = 1" // Update all rows, in case there's more than 1, or the ID=1 row got deleted before
                           // (shouldn't happen, but you never know)
        )) {
            int index = 1;

            statement.setLong(index++, earliestEventStartTimeNs);
            statement.setLong(index++, latestEventEndTimeNs);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated != 1) {
                throw new IllegalStateException("Failed to update metadata row! Rows updated: " + rowsUpdated);
            }
        }
    }
}
