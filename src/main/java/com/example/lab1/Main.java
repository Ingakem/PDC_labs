package com.example.lab1;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    int X = 4;
    int Y = 3;
    int Z = 3;
    int D = 10;
    int F = 2;
    int TOTAL = Y * Z;

    char[] VOWELS = {'а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я'};

    TextArea resultArea;
    Button startButton;
    ExecutorService executor;

    enum StoreResult {
        SUCCESS, RETRY, STOP
    }

    @Override
    public void start(Stage stage) {
        Label label = new Label(X + " производителя, " + Y + " потребителя, склад на " + D + " букв");

        startButton = new Button("Запустить");
        startButton.setOnAction(event -> startWork());

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);

        VBox root = new VBox(10, label, startButton, resultArea);
        root.setPadding(new Insets(12));
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        stage.setTitle("Лабораторная работа №5. Вариант 3");
        stage.setScene(new Scene(root, 850, 600));
        stage.show();
    }

    void startWork() {
        startButton.setDisable(true);
        resultArea.setText("Склад пуст. Начало работы.\n\n");

        Store store = new Store();
        ExecutorService pool = Executors.newFixedThreadPool(X + Y);
        executor = pool;

        for (int i = 0; i < X; i++) {
            Runnable task = new Producer("Производитель №" + (i + 1), store);
            pool.execute(task);
        }

        for (int i = 0; i < Y; i++) {
            Runnable task = new Consumer("Потребитель №" + (i + 1), store);
            pool.execute(task);
        }

        pool.shutdown();

        Thread coordinator = new Thread(() -> awaitFinish(pool, store), "Координатор");
        coordinator.start();
    }

    void awaitFinish(ExecutorService pool, Store store) {
        while (!pool.isTerminated()) {
            try {
                pool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                pool.shutdownNow();
            }
        }
        printText(store.summary());
        Platform.runLater(() -> startButton.setDisable(false));

    }

    void printText(String text) {
        Platform.runLater(() -> resultArea.appendText(text));
    }

    class Store {
        ArrayList<Character> stock = new ArrayList<>(D);
        ReentrantLock lock = new ReentrantLock(true);
        Condition notEmpty = lock.newCondition();
        Condition spaceAvailable = lock.newCondition();

        int consumedCount;
        boolean cancelled;

        StoreResult put(String name, char first, char second) {
            try {
                lock.lock();
                try {
                    if (cancelled || consumedCount >= TOTAL) {
                        return StoreResult.STOP;
                    }

                    if (stock.size() + F > D) {
                        printText(name + ": недостаточно места для двух букв. Жду.\n");
                        spaceAvailable.await();
                        return StoreResult.RETRY;
                    }

                    stock.add(first);
                    stock.add(second);
                    printText(name + " добавил: " + first + ", " + second + "\n" + stockText());

                    notEmpty.signalAll();
                    return StoreResult.SUCCESS;
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                return stopAfterInterrupt();
            }
        }

        StoreResult get(String name, int number) {
            try {
                lock.lock();
                try {
                    if (cancelled || consumedCount >= TOTAL) {
                        return StoreResult.STOP;
                    }

                    if (stock.isEmpty()) {
                        printText(name + ": склад пуст. Жду.\n");
                        notEmpty.await();
                        return StoreResult.RETRY;
                    }

                    char letter = stock.remove(0);
                    consumedCount++;
                    printText(name + " взял: " + letter + " (" + number + " из " + Z + ")\n" + stockText());

                    spaceAvailable.signalAll();
                    return StoreResult.SUCCESS;
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                return stopAfterInterrupt();
            }
        }

        StoreResult stopAfterInterrupt() {
            lock.lock();
            try {
                cancelled = true;
                notEmpty.signalAll();
                spaceAvailable.signalAll();
            } finally {
                lock.unlock();
            }

            return StoreResult.STOP;
        }

        String stockText() {
            String text = "Склад (" + stock.size() + "/" + D + "): " + stock + "\n";

            if (stock.isEmpty()) {
                text += "Склад пуст.\n";
            } else if (stock.size() == D) {
                text += "Склад полон.\n";
            }
            return text + "\n";
        }

        String summary() {
            String heading = "\nРабота прервана.\n";
            if (consumedCount == TOTAL) {
                heading = "\nВсе задачи завершили работу.\n"
                        + "Каждый потребитель получил " + Z + " буквы.\n";
            }

            int producedCount = consumedCount + stock.size();
            return heading
                    + "Всего произведено: " + producedCount + " букв.\n"
                    + "Всего получено: " + consumedCount + " букв.\n"
                    + "Осталось на складе: " + stock.size() + " букв.\n";
        }
    }

    class Producer implements Runnable {
        String name;
        Store store;
        Random random = new Random();

        Producer(String name, Store store) {
            this.name = name;
            this.store = store;
        }

        @Override
        public void run() {
            while (true) {
                char first = VOWELS[random.nextInt(VOWELS.length)];
                char second = VOWELS[random.nextInt(VOWELS.length)];

                StoreResult result;
                do {
                    result = store.put(name, first, second);
                } while (result == StoreResult.RETRY);

                if (result == StoreResult.STOP) {
                    break;
                }
            }

            printText(name + " завершил работу.\n");
        }
    }

    class Consumer implements Runnable {
        String name;
        Store store;

        Consumer(String name, Store store) {
            this.name = name;
            this.store = store;
        }

        @Override
        public void run() {
            for (int i = 1; i <= Z; i++) {
                StoreResult result;
                do {
                    result = store.get(name, i);
                } while (result == StoreResult.RETRY);

                if (result == StoreResult.STOP) {
                    return;
                }
            }

            printText(name + " получил все " + Z + " буквы и завершил работу.\n");
        }
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}