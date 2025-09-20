package com.example.reminder.ui;

import com.example.reminder.dao.ReminderDao;
import com.example.reminder.model.Reminder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

        TextField taskField = new TextField(reminder != null ? reminder.getTask() : "");
        TextField timeField = new TextField(reminder != null ? reminder.getTime() : "");
        TextField daysField = new TextField(reminder != null ? reminder.getDays() : "ALL");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("once", "daily");
        typeBox.setValue(reminder != null ? reminder.getType() : "daily");

        grid.add(new Label("Task:"), 0, 0);
        grid.add(taskField, 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        grid.add(timeField, 1, 1);
        grid.add(new Label("Days:"), 0, 2);
        grid.add(daysField, 1, 2);
        grid.add(new Label("Type:"), 0, 3);
        grid.add(typeBox, 1, 3);

        Button saveBtn = new Button("儲存");
        saveBtn.setOnAction(e -> {
            if (reminder == null) {
                dao.insert(new Reminder(
                        taskField.getText(),
                        timeField.getText(),
                        daysField.getText(),
                        typeBox.getValue()
                ));
            } else {
                reminder.setTask(taskField.getText());
                reminder.setTime(timeField.getText());
                reminder.setDays(daysField.getText());
                reminder.setType(typeBox.getValue());
                dao.update(reminder);
            }
            onSave.run();
            stage.close();
        });

        grid.add(saveBtn, 1, 4);

        Scene scene = new Scene(grid, 400, 250);
        stage.setScene(scene);
        stage.show();
    }
}
