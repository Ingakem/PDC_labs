package com.example.lab5;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class lab5 extends Application {
    private static final int X = 2, Y = 5, Z = 3, D = 12, F = 2, TOTAL = Y * Z;
    private static final int[] EVENS = {2, 4, 6, 8, 10, 12, 14, 16, 18, 20};

    private final TextArea logArea = new TextArea();
    private final ProgressBar bar = new ProgressBar(0);
    private final Label status = new Label("Склад: 0 / " + D);
    private final Button restartBtn = new Button("Перезапустить");
    private ExecutorService executor;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        bar.setMaxWidth(Double.MAX_VALUE);
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Consolas', monospace;");
        VBox.setVgrow(logArea, Priority.ALWAYS);
        restartBtn.setOnAction(e -> startSimulation());

        VBox root = new VBox(8, status, bar, logArea, restartBtn);
        root.setPadding(new Insets(10));
        stage.setTitle("ЛР №5 — Вариант 5 (Пул потоков)");
        stage.setScene(new Scene(root, 680, 480));
        stage.setOnCloseRequest(e -> { if (executor != null) executor.shutdownNow(); });
        stage.show();
        startSimulation();
    }

    private void startSimulation() {
        logArea.clear();
        restartBtn.setDisable(true);

        Thread controller = new Thread(() -> {
            BlockingQueue<Integer> buffer = new ArrayBlockingQueue<>(D, true);
            AtomicInteger produced = new AtomicInteger(), consumed = new AtomicInteger(), done = new AtomicInteger();
            Map<Integer, AtomicInteger> counters = new ConcurrentHashMap<>();
            executor = Executors.newFixedThreadPool(X + Y);

            for (int i = 1; i <= Math.max(X, Y); i++) {
                final int id = i;
                if (id <= Y) {
                    counters.put(id, new AtomicInteger());
                    executor.execute(() -> runConsumer(id, buffer, counters.get(id), consumed, done));
                }
                if (id <= X) {
                    executor.execute(() -> runProducer(id, buffer, produced, done));
                }
            }

            executor.shutdown();
            while (done.get() < Y) Thread.yield();
            executor.shutdownNow();
            while (!executor.isTerminated()) Thread.yield();

            StringBuilder report = new StringBuilder("\nИтоговый результат:\n")
                    .append("Произведено: ").append(produced.get())
                    .append(" | Потреблено: ").append(consumed.get()).append("\n");
            counters.forEach((id, c) -> report.append("Потребитель №").append(id).append(": ").append(c.get()).append("\n"));
            log(report.append("Все потоки завершены").toString(), buffer.size());
            Platform.runLater(() -> restartBtn.setDisable(false));
        });
        controller.start();
    }

    private void runProducer(int id, BlockingQueue<Integer> buf, AtomicInteger produced, AtomicInteger done) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        try {
            while (produced.get() < TOTAL && done.get() < Y) {
                if (buf.remainingCapacity() < F) log("[Производитель №" + id + "] Склад полон, ожидание...", buf.size());
                int a = EVENS[rnd.nextInt(EVENS.length)], b = EVENS[rnd.nextInt(EVENS.length)];
                buf.put(a);
                buf.put(b);
                int p = produced.addAndGet(F), size = buf.size();
                log("[Производитель №" + id + "] произвел: " + a + ", " + b + " | Всего: " + p + " | Склад: " + size + "/" + D + " " + buf, size);
                Thread.yield();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log("[Производитель №" + id + "] завершил работу", buf.size());
    }

    private void runConsumer(int id, BlockingQueue<Integer> buf, AtomicInteger personal, AtomicInteger consumed, AtomicInteger done) {
        try {
            while (personal.get() < Z) {
                if (buf.isEmpty()) log("[Потребитель №" + id + "] Склад пуст, ожидание...", buf.size());
                int item = buf.take(), p = personal.incrementAndGet(), c = consumed.incrementAndGet(), size = buf.size();
                log("[Потребитель №" + id + "] взял: " + item + " | Лично: " + p + "/" + Z + " | Всего: " + c + " | Склад: " + size + "/" + D + (size == 0 ? " (Склад пуст)" : " " + buf), size);
                Thread.yield();
            }
            done.incrementAndGet();
            log("[Потребитель №" + id + "] удовлетворен (" + Z + " объекта)", buf.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void log(String msg, int size) {
        System.out.println(msg);
        Platform.runLater(() -> {
            logArea.appendText(msg + "\n");
            bar.setProgress((double) size / D);
            status.setText("Склад: " + size + " / " + D + (size == 0 ? " (Склад пуст)" : size >= D ? " (Склад полон)" : ""));
        });
    }
}