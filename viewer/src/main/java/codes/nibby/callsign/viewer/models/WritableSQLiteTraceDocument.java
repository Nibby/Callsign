package codes.nibby.callsign.viewer.models;

import codes.nibby.callsign.api.Event;
import codes.nibby.callsign.api.InstantEvent;
import codes.nibby.callsign.api.TimedEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static codes.nibby.callsign.viewer.models.SQLiteTraceDocument.Schema.*;

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
            statement.execute(
                "CREATE TABLE " + EventsTable.TABLE_NAME +
                "(" +
                    EventsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    EventsTable.COLUMN_EVENT_TYPE + " TEXT NOT NULL," +
                    EventsTable.COLUMN_EVENT_NAME + " TEXT NOT NULL," +
                    EventsTable.COLUMN_START_TIME_NS + " INTEGER NULL," +
                    EventsTable.COLUMN_END_TIME_NS + " INTEGER NULL" +
                ")"
            );

            statement.execute("CREATE INDEX index_event_name ON event_data (event_name)");
            statement.execute("CREATE INDEX index_start_time_ns ON event_data (start_time_ns)");
            statement.execute("CREATE INDEX index_end_time_ns ON event_data (end_time_ns)");

            statement.execute(
                "CREATE TABLE " + AttributeHeaderTable.TABLE_NAME +
                "(" +
                    AttributeHeaderTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AttributeHeaderTable.COLUMN_COLUMN_NAME + " TEXT NOT NULL," +
                    AttributeHeaderTable.COLUMN_ATTRIBUTE_NAME + " TEXT NOT NULL" +
                ")"
            );

            statement.execute("CREATE INDEX index_column_name ON attribute_name_lookup (column_name)");
            statement.execute("CREATE INDEX index_attribute_name ON attribute_name_lookup (attribute_name)");
        }
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
                String columnName = resultSet.getString("column_name");
                String attributeName = resultSet.getString("attribute_name");

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
                    createMissingAttributeNames(missingAttributeNames);
                }

                appendEventImpl(event);
            } catch (SQLException e) {
                throw new IOException(e);
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
                    "ALTER TABLE " + EventsTable.TABLE_NAME
                        + " ADD COLUMN " + newColumnName + " TEXT NULL"
                );
            }
        }
    }

    private void appendEventImpl(Event event) throws SQLException {
        StringBuilder additionalAttributeColumns = new StringBuilder();
        StringBuilder additionalAttributeValues = new StringBuilder();

        Map<Integer, String> parameterOffsetToAttributeName = new HashMap<>(attributeNameLookup.size());

        int i = 0;
        for (var entry  : attributeNameLookup.entrySet()) {
            String columnName = entry.getKey();
            String attributeName = entry.getValue();

            additionalAttributeColumns.append(", ").append(columnName);
            additionalAttributeValues.append(", ?");

            parameterOffsetToAttributeName.put(i, attributeName);

            i++;
        }

        try (
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + EventsTable.TABLE_NAME + "(" +
                    EventsTable.COLUMN_EVENT_TYPE + ", " +
                    EventsTable.COLUMN_EVENT_NAME + ", " +
                    EventsTable.COLUMN_START_TIME_NS + ", " +
                    EventsTable.COLUMN_END_TIME_NS +
                    additionalAttributeColumns + ") " +
                    "VALUES (?, ?, ?, ?" + additionalAttributeValues + ")"
            )
        ) {
            statement.setString(1, event.getType());
            statement.setString(2, event.getName());

            if (event instanceof TimedEvent timedEvent) {
                long startTimeNs = timedEvent.getStartTimeNs() == null ? Long.MIN_VALUE : timedEvent.getStartTimeNs();
                statement.setLong(3, startTimeNs);

                long endTimeNs = timedEvent.getEndTimeNs() == null ? Long.MAX_VALUE : timedEvent.getEndTimeNs();
                statement.setLong(4, endTimeNs);
            } else if (event instanceof InstantEvent instantEvent) {
                statement.setLong(3, instantEvent.getTimeNs());
                statement.setObject(4, null);
            } else {
                throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
            }

            for (var attributeEntry : parameterOffsetToAttributeName.entrySet()) {
                int offset = attributeEntry.getKey();
                String attributeName = attributeEntry.getValue();

                statement.setString(5 + offset, event.getAttribute(attributeName));
            }

            statement.execute();
        }
    }
}
