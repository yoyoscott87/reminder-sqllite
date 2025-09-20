package com.example.reminder.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;	

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
            try (Connection connection = DriverManager.getConnection(DB_URL);
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS reminders (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "title TEXT NOT NULL, " +
                                "description TEXT NOT NULL DEFAULT '', " +
                                "remind_at INTEGER NOT NULL" +
                                ")");
                initialized = true;
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        initializeDatabaseIfNeeded();
        return DriverManager.getConnection(DB_URL);
    }
}
