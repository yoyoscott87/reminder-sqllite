package com.example.reminder.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class Database {
	private static final String DB_URL = "jdbc:sqlite:reminder.db";
    private static final Object INIT_LOCK = new Object();
    private static volatile boolean initialized = false;

    static {
        try {
            Class.forName("org.sqlite.JDBC"); // 載入驅動
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabaseIfNeeded() throws SQLException {
        if (initialized) {
            return;
        }
        synchronized (INIT_LOCK) {
            if (initialized) {
                return;
            }
            try (Connection connection = DriverManager.getConnection(DB_URL)) {
                ensureSchema(connection);
                initialized = true;
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        initializeDatabaseIfNeeded();
        return DriverManager.getConnection(DB_URL);
    }

    private static void ensureSchema(Connection connection) throws SQLException {
        if (!doesTableExist(connection, "reminders")) {
            createRemindersTable(connection);
            return;
        }

        Set<String> columns = getTableColumns(connection, "reminders");
        boolean hasNewSchema = columns.contains("task") &&
                columns.contains("time") &&
                columns.contains("days") &&
                columns.contains("type");

        if (hasNewSchema) {
            return;
        }

        migrateOldSchema(connection, columns);
    }

    private static boolean doesTableExist(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static Set<String> getTableColumns(Connection connection, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }
        return columns;
    }

    private static void createRemindersTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reminders (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "task TEXT NOT NULL, " +
                            "time TEXT NOT NULL, " +
                            "days TEXT NOT NULL DEFAULT 'ALL', " +
                            "type TEXT NOT NULL" +
                            ")");
        }
    }

    private static void migrateOldSchema(Connection connection, Set<String> existingColumns) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS reminders_old");
            statement.executeUpdate("ALTER TABLE reminders RENAME TO reminders_old");
        }

        createRemindersTable(connection);

        if (existingColumns.contains("title") && existingColumns.contains("remind_at")) {
            String copySql = "INSERT INTO reminders (task, time, days, type) " +
                    "SELECT title, CAST(remind_at AS TEXT), 'ALL', 'once' FROM reminders_old";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(copySql);
            } catch (SQLException e) {
                System.err.println("Failed to migrate old reminder data: " + e.getMessage());
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS reminders_old");
        }
    }
}
