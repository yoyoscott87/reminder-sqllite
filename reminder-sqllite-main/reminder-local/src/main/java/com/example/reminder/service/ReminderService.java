package com.example.reminder.service;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.reminder.dao.Database;
import com.example.reminder.ui.ReminderPopup;

public class ReminderService {
    // 當前時間窗防抖 (避免同一分鐘多次跳)
    private static final Set<String> remindedToday = new HashSet<>();
    // 每天只提醒一次 (同一個提醒 ID)
    private static final Map<String, Integer> remindCountToday = new HashMap<>();

    private static LocalDate lastCheckedDate = LocalDate.now();

    private static final DateTimeFormatter ONCE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DAILY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void checkAndNotify() {
        // 跨日清空
        if (!LocalDate.now().equals(lastCheckedDate)) {
            remindedToday.clear();
            remindCountToday.clear();
            lastCheckedDate = LocalDate.now();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime nowOnlyTime = now.toLocalTime();
        DayOfWeek today = now.getDayOfWeek();

        String sql = "SELECT id, task, time, days, type FROM reminders";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String task = safe(rs.getString("task"));
                String timeStr = safe(rs.getString("time"));
                String dayStr = rs.getString("days") != null ? rs.getString("days").trim().toUpperCase() : "ALL";
                String type = safe(rs.getString("type")).toLowerCase();

                try {
                    if ("once".equals(type)) {
                        LocalDateTime remindTime = LocalDateTime.parse(timeStr, ONCE_FORMATTER);
                        long diffMin = Duration.between(remindTime, now).toMinutes();

                        String debounceKey = "ONCE-" + id + "-" + remindTime.toString();
                        String dailyKey = "ONCE-" + id + "-" + LocalDate.now();

                        if (Math.abs(diffMin) <= 1 && shouldNotify(debounceKey, dailyKey)) {
                            ReminderPopup.show("📌 (一次性) " + task);
                            markNotified(debounceKey, dailyKey);
                        }

                    } else if ("daily".equals(type)) {
                        if (!isDayMatch(dayStr, today)) continue;

                        LocalTime remindTime = LocalTime.parse(timeStr, DAILY_FORMATTER);
                        long diffMin = Duration.between(remindTime, nowOnlyTime).toMinutes();

                        String debounceKey = "DAILY-" + id + "-" + remindTime + "-" + LocalDate.now();
                        String dailyKey = "DAILY-" + id + "-" + LocalDate.now();

                        if (Math.abs(diffMin) <= 1 && shouldNotify(debounceKey, dailyKey)) {
                            ReminderPopup.show("📌 (每日) " + task);
                            markNotified(debounceKey, dailyKey);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("⚠️ 無法解析時間格式: " + timeStr + "，錯誤訊息: " + ex.getMessage());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldNotify(String debounceKey, String dailyKey) {
        // 今天已經提醒過這個 ID → 不再提醒
        if (remindCountToday.containsKey(dailyKey)) return false;
        // 同一分鐘內已提醒 → 不再提醒
        if (remindedToday.contains(debounceKey)) return false;
        return true;
    }

    private void markNotified(String debounceKey, String dailyKey) {
        remindedToday.add(debounceKey);
        remindCountToday.put(dailyKey, 1);
    }

    private boolean isDayMatch(String dayStr, DayOfWeek today) {
        if (dayStr.equals("ALL")) return true;
        if (dayStr.equals("WEEKDAY")) return today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY;
        if (dayStr.equals("WEEKEND")) return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;

        String todayAbbr = today.toString().substring(0, 3);
        for (String d : dayStr.split(",")) {
            if (d.trim().equalsIgnoreCase(todayAbbr)) {
                return true;
            }
        }
        return false;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
