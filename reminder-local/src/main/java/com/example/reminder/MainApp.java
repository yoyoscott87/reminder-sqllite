package com.example.reminder;

import com.example.reminder.ui.ReminderManager;
import com.example.reminder.service.ReminderService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class MainApp extends Application {
    private ReminderService checker;
    private Timer timer;

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(false);
        // 啟動主視窗 (提醒管理器)
        ReminderManager manager = new ReminderManager();
        try {
            manager.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 初始化提醒檢查器
        checker = new ReminderService();

        // 每分鐘檢查一次
        timer = new Timer(true); // daemon 執行緒
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checker.checkAndNotify(); // ← 這裡會呼叫 ReminderPopup.show()
            }
        }, 0, 10 * 1000);
    }

    @Override
    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
