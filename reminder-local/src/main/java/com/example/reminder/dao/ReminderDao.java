package com.example.reminder.dao;


import com.example.reminder.model.Reminder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReminderDao {

    public void insert(Reminder reminder) {
        String sql = "INSERT INTO reminders (task, time, days, type) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reminder.getTask());
            pstmt.setString(2, reminder.getTime());
            pstmt.setString(3, reminder.getDays());
            pstmt.setString(4, reminder.getType());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Reminder> getAll() {
        List<Reminder> list = new ArrayList<>();
        String sql = "SELECT * FROM reminders";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Reminder r = new Reminder(
                        rs.getInt("id"),
                        rs.getString("task"),
                        rs.getString("time"),
                        rs.getString("days"),
                        rs.getString("type")
                );
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void update(Reminder reminder) {
        String sql = "UPDATE reminders SET task=?, time=?, days=?, type=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reminder.getTask());
            pstmt.setString(2, reminder.getTime());
            pstmt.setString(3, reminder.getDays());
            pstmt.setString(4, reminder.getType());
            pstmt.setInt(5, reminder.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM reminders WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
