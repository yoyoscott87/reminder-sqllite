package com.example.reminder;

import com.example.reminder.ui.ReminderManager;
import com.example.reminder.service.ReminderService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Timer;
import java.util.TimerTask;

public class MainApp extends Application {
    private ReminderService checker;
    private Timer timer;
    private static ServerSocket lockSocket; // 單一實例鎖
    private Stage mainStage;

    public static void main(String[] args) {
        // 單一實例鎖 (固定埠號)
        try {
            lockSocket = new ServerSocket(56789);
        } catch (IOException e) {
            System.out.println("⚠️ 程式已在執行中！");
            return; // 不再啟動新的實例
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(false); // 關閉視窗仍常駐

        this.mainStage = stage;

        // 啟動主視窗
        ReminderManager manager = new ReminderManager();
        try {
            manager.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 初始化提醒檢查器
        checker = new ReminderService();

        // 每分鐘檢查一次
        timer = new Timer(true); // daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checker.checkAndNotify();
            }
        }, 0, 60 * 1000);

        // 建立系統托盤
        setupSystemTray();
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("⚠️ 系統不支援托盤！");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage("icon.png"); // 請放一張小圖示

        PopupMenu popup = new PopupMenu();

        // 打開視窗
        MenuItem openItem = new MenuItem("打開");
        openItem.addActionListener(e -> Platform.runLater(() -> {
            if (mainStage != null) {
                mainStage.show();
                mainStage.toFront();
            }
        }));
        popup.add(openItem);

        // 退出程式
        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> {
            stopApp();
        });
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "提醒程式", popup);
        trayIcon.setImageAutoSize(true);

        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            if (mainStage != null) {
                mainStage.show();
                mainStage.toFront();
            }
        }));

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        stopApp();
    }

    private void stopApp() {
        if (timer != null) {
            timer.cancel();
        }
        try {
            if (lockSocket != null) {
                lockSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.exit();
        System.exit(0);
    }
}
