package com.example.reminder.ui;

import com.example.reminder.dao.ReminderDao;
import com.example.reminder.model.Reminder;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ReminderManager extends Application {
    private TableView<Reminder> table;
    private ObservableList<Reminder> data;
    private ReminderDao dao = new ReminderDao();

    @Override
    public void start(Stage stage) {
        table = new TableView<>();
        data = FXCollections.observableArrayList(dao.getAll());
        table.setItems(data);

        TableColumn<Reminder, String> taskCol = new TableColumn<>("Task");
        taskCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTask()));

        TableColumn<Reminder, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTime()));

        TableColumn<Reminder, String> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDays()));

        TableColumn<Reminder, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getType()));

        table.getColumns().addAll(taskCol, timeCol, daysCol, typeCol);

        // 按鈕列
        Button addBtn = new Button("新增");
        addBtn.setOnAction(e -> openForm(null));

        Button editBtn = new Button("修改");
        editBtn.setOnAction(e -> {
            Reminder selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openForm(selected);
        });

        Button deleteBtn = new Button("刪除");
        deleteBtn.setOnAction(e -> {
            Reminder selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dao.delete(selected.getId());
                refreshTable();
            }
        });

        HBox buttons = new HBox(10, addBtn, editBtn, deleteBtn);

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(buttons);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("提醒管理器");
        stage.show();
    }

    private void refreshTable() {
        data.setAll(dao.getAll());
    }

    private void openForm(Reminder reminder) {
        ReminderForm form = new ReminderForm(reminder, dao, this::refreshTable);
        form.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
