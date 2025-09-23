package com.example.reminder.ui;

import com.example.reminder.dao.ReminderDao;
import com.example.reminder.model.Reminder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReminderForm {
    private Reminder reminder;
    private ReminderDao dao;
    private Runnable onSave;

    public ReminderForm(Reminder reminder, ReminderDao dao, Runnable onSave) {
        this.reminder = reminder;
        this.dao = dao;
        this.onSave = onSave;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(reminder == null ? "新增提醒" : "修改提醒");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Task
        TextField taskField = new TextField(reminder != null ? reminder.getTask() : "");

        // Type
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("once", "daily");
        typeBox.setValue(reminder != null ? reminder.getType() : "daily");

        // Date (only for once)
        DatePicker datePicker = new DatePicker();
        datePicker.setDisable(true);

        // Hour / Minute
        ComboBox<Integer> hourBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) hourBox.getItems().add(i);
        ComboBox<Integer> minuteBox = new ComboBox<>();
        for (int i = 0; i < 60; i++) minuteBox.getItems().add(i);

        // Days (multi-select for daily, using ControlsFX)
        CheckComboBox<String> daysBox = new CheckComboBox<>();
        daysBox.getItems().addAll("MON", "TUE", "WED", "THUR", "FRI", "SAT", "SUN", "ALL");

        // 預設值
        if (reminder != null) {
            if ("once".equals(reminder.getType())) {
                try {
                    String[] parts = reminder.getTime().split(" ");
                    LocalDate d = LocalDate.parse(parts[0]);
                    datePicker.setValue(d);
                    String[] hm = parts[1].split(":");
                    hourBox.setValue(Integer.parseInt(hm[0]));
                    minuteBox.setValue(Integer.parseInt(hm[1]));
                } catch (Exception ignored) {}
            } else { // daily
                try {
                    String[] hm = reminder.getTime().split(":");
                    hourBox.setValue(Integer.parseInt(hm[0]));
                    minuteBox.setValue(Integer.parseInt(hm[1]));
                } catch (Exception ignored) {}

                for (String d : reminder.getDays().split(",")) {
                    daysBox.getCheckModel().check(d.trim().toUpperCase());
                }
            }
        }

        // Type 切換
        typeBox.setOnAction(e -> {
            if ("once".equals(typeBox.getValue())) {
                datePicker.setDisable(false);
                daysBox.setDisable(true);
            } else {
                datePicker.setDisable(true);
                daysBox.setDisable(false);
            }
        });

        // Layout
        grid.add(new Label("Task:"), 0, 0);
        grid.add(taskField, 1, 0);

        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeBox, 1, 1);

        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);

        grid.add(new Label("Hour:"), 0, 3);
        grid.add(hourBox, 1, 3);

        grid.add(new Label("Minute:"), 0, 4);
        grid.add(minuteBox, 1, 4);

        grid.add(new Label("Days:"), 0, 5);
        grid.add(daysBox, 1, 5);

        // Save Button
        Button saveBtn = new Button("儲存");
        saveBtn.setOnAction(e -> {
            String task = taskField.getText();
            String type = typeBox.getValue();
            String time;
            String days;

            if ("once".equals(type)) {
                LocalDate d = datePicker.getValue();
                if (d == null || hourBox.getValue() == null || minuteBox.getValue() == null) {
                    showAlert("請完整選擇日期與時間");
                    return;
                }
                time = d.toString() + " " +
                        String.format("%02d:%02d", hourBox.getValue(), minuteBox.getValue());
                days = "ALL"; // once 預設 ALL
            } else {
                if (hourBox.getValue() == null || minuteBox.getValue() == null) {
                    showAlert("請選擇時間");
                    return;
                }
                time = String.format("%02d:%02d", hourBox.getValue(), minuteBox.getValue());

                List<String> selected = daysBox.getCheckModel().getCheckedItems();
                if (selected.isEmpty()) {
                    showAlert("請至少選擇一天");
                    return;
                }
                days = selected.stream().collect(Collectors.joining(","));
            }

            if (reminder == null) {
                dao.insert(new Reminder(task, time, days, type));
            } else {
                reminder.setTask(task);
                reminder.setTime(time);
                reminder.setDays(days);
                reminder.setType(type);
                dao.update(reminder);
            }

            onSave.run();
            stage.close();
        });

        grid.add(saveBtn, 1, 6);

        Scene scene = new Scene(grid, 500, 320);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
