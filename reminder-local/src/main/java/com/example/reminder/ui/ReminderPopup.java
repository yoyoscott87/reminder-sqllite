package com.example.reminder.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ReminderPopup {

    private static Stage currentStage; // 避免重複開太多

    /**
     * 顯示提醒視窗
     * @param msg 提醒訊息
     * @param autoCloseSec 幾秒後自動關閉 (0 = 不自動關閉)
     */
    public static void show(String msg, int autoCloseSec) {
        Platform.runLater(() -> {
            if (currentStage != null && currentStage.isShowing()) {
                currentStage.close(); // 關掉舊的
            }

            Stage stage = new Stage();
            currentStage = stage;
            stage.initStyle(StageStyle.TRANSPARENT);

            // 文字
            Label label = new Label(msg);
            label.setWrapText(true);
            label.setMaxWidth(600);
            label.setStyle(
                "-fx-font-size: 32px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
            );

            // 按鈕
            Button okBtn = new Button("確定");
            final String NORMAL =
                "-fx-font-size: 18px;" +
                "-fx-background-color: #8EB69B;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 10 24;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);";
            final String HOVER = NORMAL.replace("#8EB69B", "#8db584");
            final String PRESSED = NORMAL.replace("#8EB69B", "#7fb374");

            okBtn.setStyle(NORMAL);
            okBtn.setOnMouseEntered(e -> okBtn.setStyle(HOVER));
            okBtn.setOnMouseExited(e -> okBtn.setStyle(NORMAL));
            okBtn.setOnMousePressed(e -> okBtn.setStyle(PRESSED));
            okBtn.setOnMouseReleased(e -> okBtn.setStyle(HOVER));
            okBtn.setOnAction(e -> stage.close());

            // 容器
            VBox root = new VBox(25, label, okBtn);
            root.setAlignment(Pos.CENTER);
            root.setStyle(
                "-fx-background-color: #79ba8e;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 30;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.5, 0, 0);"
            );

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
            stage.centerOnScreen();
            stage.show();

            // 淡入動畫
            FadeTransition ft = new FadeTransition(Duration.millis(400), root);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

            // 自動關閉
            if (autoCloseSec > 0) {
                PauseTransition delay = new PauseTransition(Duration.seconds(autoCloseSec));
                delay.setOnFinished(ev -> stage.close());
                delay.play();
            }
        });
    }

    // 預設 10 秒自動關閉
    public static void show(String msg) {
        show(msg, 10);
    }
}
