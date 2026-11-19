package com.example.lab1;

import java.util.ArrayList;
import java.util.Random;
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

    char[] VOWELS = {'а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я'};

    TextArea resultArea;
    Button startButton;
    Store store;

    Thread[] producers = new Thread[X];
    Thread[] consumers = new Thread[Y];
    Thread coordinator;

    enum StoreResult {
        SUCCESS, RETRY, STOP
    }

    @Override
    public void start(Stage stage) {
        Label label = new Label("4 производителя, 3 потребителя, склад на 10 букв");

        startButton = new Button("Запустить");
        startButton.setOnAction(event -> startWork());

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);

        VBox root = new VBox(10, label, startButton, resultArea);
        root.setPadding(new Insets(12));
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        stage.setTitle("Лабораторная работа №4. Вариант 3");
        stage.setScene(new Scene(root, 850, 600));
        stage.show();
    }

    void startWork() {
        resultArea.clear();
        startButton.setDisable(true);
        store = new Store();

        printText("Склад пуст. Начало работы.\n\n");

        for (int i = 0; i < X; i++) {
            producers[i] = new Producer("Производитель №" + (i + 1));
        }

        for (int i = 0; i < Y; i++) {
            consumers[i] = new Consumer("Потребитель №" + (i + 1));
        }

        for (int i = 0; i < producers.length; i++) {
            producers[i].start();
        }

        for (int i = 0; i < consumers.length; i++) {
            consumers[i].start();
        }

        coordinator = new Thread(() -> {
            try {
                for (int i = 0; i < consumers.length; i++) {
                    consumers[i].join();
                }

                for (int i = 0; i < producers.length; i++) {
                    producers[i].join();
                }

                printText("\nВсе потоки завершили работу.\n"
                        + "Каждый потребитель получил " + Z + " буквы.\n"
                        + "Всего получено: " + store.consumedCount + " букв.\n"
                        + "Осталось на складе: " + store.size() + " букв.\n");

                Platform.runLater(() -> startButton.setDisable(false));
            } catch (InterruptedException e) {
                printText("\nКоординатор был прерван.\n");
                e.printStackTrace();
            }
        }, "Координатор");

        coordinator.start();
    }

    void printText(String text) {
        Platform.runLater(() -> resultArea.appendText(text));
    }

    class Store {
        ArrayList<Character> stock = new ArrayList<>();
        int consumedCount = 0;

        synchronized StoreResult put(String name, char first, char second) {
            if (consumedCount >= Y * Z) {
                return StoreResult.STOP;
            } else if (stock.size() + F > D) {
                printText(name + ": недостаточно места для двух букв. Жду.\n");
                try {
                    wait();
                } catch (InterruptedException e) {
                    printText("\nОжидание было прервано.\n");
                    e.printStackTrace();
                    return StoreResult.STOP;
                }
                return StoreResult.RETRY;
            } else {
                stock.add(first);
                stock.add(second);

                printText(name + " добавил: " + first + ", " + second + "\n");
                showStock();

                notifyAll();
                return StoreResult.SUCCESS;
            }
        }

        synchronized StoreResult get(String name, int number) {
            if (consumedCount >= Y * Z) {
                return StoreResult.STOP;
            } else if (stock.isEmpty()) {
                printText(name + ": склад пуст. Жду.\n");
                try {
                    wait();
                } catch (InterruptedException e) {
                    printText("\nОжидание было прервано.\n");
                    e.printStackTrace();
                    return StoreResult.STOP;
                }
                return StoreResult.RETRY;
            } else {
                char letter = stock.remove(0);
                consumedCount++;

                printText(name + " взял: " + letter + " (" + number + " из " + Z + ")\n");
                showStock();

                notifyAll();
                return StoreResult.SUCCESS;
            }
        }

        void showStock() {
            printText("Склад (" + stock.size() + "/" + D + "): " + stock + "\n");

            if (stock.isEmpty()) {
                printText("Склад пуст.\n");
            } else if (stock.size() == D) {
                printText("Склад полон.\n");
            }

            printText("\n");
        }

        int size() {
            return stock.size();
        }
    }

    class Producer extends Thread {
        Random random = new Random();

        Producer(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                char first = VOWELS[random.nextInt(VOWELS.length)];
                char second = VOWELS[random.nextInt(VOWELS.length)];

                StoreResult result;
                do {
                    result = store.put(getName(), first, second);
                } while (result == StoreResult.RETRY);

                if (result == StoreResult.STOP) {
                    break;
                }
            }

            printText(getName() + " завершил работу.\n");
        }
    }

    class Consumer extends Thread {
        Consumer(String name) {
            super(name);
        }
        @Override
        public void run() {
            for (int i = 1; i <= Z; i++) {
                StoreResult result;
                do {
                    result = store.get(getName(), i);
                } while (result == StoreResult.RETRY);

                if (result == StoreResult.STOP) {
                    return;
                }
            }

            printText(getName() + " получил все " + Z + " буквы и завершил работу.\n");
        }
    }

    @Override
    public void stop() {
        if (coordinator != null) {
            coordinator.interrupt();
        }

        for (int i = 0; i < producers.length; i++) {
            if (producers[i] != null) {
                producers[i].interrupt();
            }
        }

        for (int i = 0; i < consumers.length; i++) {
            if (consumers[i] != null) {
                consumers[i].interrupt();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}