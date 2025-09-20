package com.example.reminder.service;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import com.example.reminder.dao.Database;
import com.example.reminder.ui.ReminderPopup;

public class ReminderService {
    private static final Set<String> remindedToday = new HashSet<>();
    private static LocalDate lastCheckedDate = LocalDate.now();

    /**
     * 核心檢查方法：從 SQLite 讀取 reminders 表，逐一判斷是否需要提醒
     */
    public void checkAndNotify() {
        // 每天凌晨清空記錄
        if (!LocalDate.now().equals(lastCheckedDate)) {
            remindedToday.clear();
            lastCheckedDate = LocalDate.now();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime nowOnlyTime = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        String sql = "SELECT id, task, time, days, type FROM reminders";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String timeStr = rs.getString("time").trim();
                String task = rs.getString("task");
                String dayStr = rs.getString("days") != null ? rs.getString("days").trim().toUpperCase() : "ALL";
                String type = rs.getString("type");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");
                try {
                    // 🟢 一次性提醒 (格式 yyyy/M/d HH:mm，不檢查星期)
                    if ("once".equalsIgnoreCase(type) || timeStr.contains("/")) {
                        LocalDateTime remindTime = LocalDateTime.parse(timeStr, formatter);
                        long diff = Duration.between(remindTime, now).toMinutes();

                        String key = "ONCE-" + remindTime.toString();
                        if (Math.abs(diff) <= 1 && !remindedToday.contains(key)) {
                            ReminderPopup.show("📌 (一次性) " + task);
                            remindedToday.add(key);
                        }
                    }
                    // 🟢 每日提醒 (格式 HH:mm，要檢查星期)
                    else {
                        if (!isDayMatch(dayStr, today)) {
                            continue; // 星期不符合 → 跳過
                        }

                        LocalTime remindTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                        long diff = Duration.between(remindTime, nowOnlyTime).toMinutes();
                        if (Math.abs(diff) <= 1) {
                            String key = remindTime.toString() + "-" + LocalDate.now();
                            if (!remindedToday.contains(key)) {
                                ReminderPopup.show("📌 (每日) " + task);
                                remindedToday.add(key);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ 無法解析時間格式: " + timeStr + "，錯誤訊息: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判斷今天是否符合 row 設定的星期
     */
    private boolean isDayMatch(String dayStr, DayOfWeek today) {
        if (dayStr.equals("ALL")) return true;
        if (dayStr.equals("WEEKDAY")) return today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY;
        if (dayStr.equals("WEEKEND")) return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;

        String[] days = dayStr.split(",");
        for (String d : days) {
            if (d.trim().equalsIgnoreCase(today.toString())) {
                return true;
            }
        }
        return false;
    }
}
