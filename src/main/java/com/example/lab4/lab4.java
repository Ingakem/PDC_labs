package com.example.lab4;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class lab4 extends Application {
    public static final int X = 4, Y = 5, Z = 3, D = 12;

    private TextArea logArea;
    private ProgressBar capacityBar;
    private Label statusLabel;
    private Button restartBtn;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        statusLabel = new Label("Склад: 0 / " + D);
        capacityBar = new ProgressBar(0);
        capacityBar.setMaxWidth(Double.MAX_VALUE);
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Consolas', monospace;");
        VBox.setVgrow(logArea, Priority.ALWAYS);
        restartBtn = new Button("Перезапустить");
        restartBtn.setOnAction(e -> startSimulation());
        VBox root = new VBox(8, statusLabel, capacityBar, logArea, restartBtn);
        root.setPadding(new Insets(10));
        stage.setTitle("ЛР №4 — Вариант 5 (Производитель-Потребитель)");
        stage.setScene(new Scene(root, 640, 460));
        stage.show();
        startSimulation();
    }

    private void startSimulation() {
        logArea.clear();
        restartBtn.setDisable(true);

        Thread controller = new Thread(() -> {
            Store store = new Store(this);
            Producer[] producers = new Producer[X];
            Consumer[] consumers = new Consumer[Y];

            for (int i = 0; i < X; i++) {
                producers[i] = new Producer(store, "Производитель №" + (i + 1));
            }
            for (int i = 0; i < Y; i++) {
                consumers[i] = new Consumer(store, "Потребитель №" + (i + 1));
            }

            int max = Math.max(X, Y);
            for (int i = 0; i < max; i++) {
                if (i < Y) consumers[i].start();
                if (i < X) producers[i].start();
            }

            for (Consumer c : consumers) {
                while (c.isAlive()) {
                    Thread.yield();
                }
            }

            store.stop();
            store.log("Все потоки завершены");
            Platform.runLater(() -> restartBtn.setDisable(false));
        });
        controller.start();
    }

    public void updateUI(String message, int currentSize) {
        System.out.println(message);
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            capacityBar.setProgress((double) currentSize / D);
            statusLabel.setText("Склад: " + currentSize + " / " + D
                    + (currentSize == 0 ? " (Склад пуст)" : currentSize >= D ? " (Склад полон)" : ""));
        });
    }
}

class Store {
    private final ArrayList<Integer> stockList = new ArrayList<>();
    private final lab4 app;
    private boolean active = true;
    private String lastProducer = "";
    private String lastConsumer = "";

    public Store(lab4 app) {
        this.app = app;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void stop() {
        active = false;
        notifyAll();
    }

    public synchronized void log(String msg) {
        app.updateUI(msg, stockList.size());
    }

    private void awaitMonitor(long ms) {
        try {
            if (ms > 0) wait(ms);
            else wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logStockState() {
        if (stockList.isEmpty()) {
            log("Склад пуст");
        } else {
            StringBuilder sb = new StringBuilder("На складе имеется " + stockList.size() + " единиц -> ");
            for (int num : stockList) sb.append(num).append(" ");
            if (stockList.size() >= lab4.D) sb.append("(Склад полон)");
            log(sb.toString());
        }
    }

    public synchronized void get(String name) {
        if (name.equals(lastConsumer)) {
            awaitMonitor(5);
        }
        while (stockList.isEmpty()) {
            log("Склад пуст. " + name + " ждет...");
            awaitMonitor(0);
        }
        lastConsumer = name;
        int val = stockList.remove(stockList.size() - 1);
        log(name + " взял со склада: " + val);
        logStockState();
        notifyAll();
        awaitMonitor((int) (Math.random() * 10) + 1);
    }

    public synchronized void put(String name, int a, int b) {
        if (name.equals(lastProducer) && active) {
            awaitMonitor(5);
        }
        while (stockList.size() + 2 > lab4.D && active) {
            log("Склад полон. " + name + " ждет...");
            awaitMonitor(0);
        }
        if (!active) return;

        lastProducer = name;
        stockList.add(a);
        stockList.add(b);
        log(name + " поместил в хранилище два числа: " + a + ", " + b);
        logStockState();
        notifyAll();
        awaitMonitor((int) (Math.random() * 10) + 1);
    }
}

class Producer extends Thread {
    private final Store s;
    private final int[] evenNumbers = {2, 4, 6, 8, 10, 12, 14, 16, 18, 20};

    public Producer(Store s, String name) {
        super(name);
        this.s = s;
    }

    @Override
    public void run() {
        Thread.yield();
        while (s.isActive()) {
            int a = evenNumbers[(int) (Math.random() * evenNumbers.length)];
            int b = evenNumbers[(int) (Math.random() * evenNumbers.length)];
            s.put(getName(), a, b);
            Thread.yield();
        }
    }
}

class Consumer extends Thread {
    private final Store s;

    public Consumer(Store s, String name) {
        super(name);
        this.s = s;
    }

    @Override
    public void run() {
        Thread.yield();
        for (int i = 0; i < lab4.Z; i++) {
            s.get(getName());
            Thread.yield();
        }
        s.log(getName() + " взял " + lab4.Z + " числа. Поток завершен");
    }
}