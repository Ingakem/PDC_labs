package com.example.lab3;

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

public class Main1 extends Application {

    int[] mas = new int[100];
    TextArea resultArea;
    Button startButton;

    Thread th1;
    Thread th2;
    Thread th3;
    Thread th4;
    Thread coordinator;

    @Override
    public void start(Stage stage) {
        Label label = new Label("Лабораторная работа №3. Вариант 3");

        startButton = new Button("Запустить");
        startButton.setOnAction(event -> startWork());

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        VBox root = new VBox(10, label, startButton, resultArea);
        root.setPadding(new Insets(10));

        stage.setTitle("Лабораторная работа №3. Вариант 3");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    void startWork() {
        startButton.setDisable(true);

        Random random = new Random();
        StringBuilder text = new StringBuilder("Исходный массив (100 элементов):\n");

        for (int i = 0; i < mas.length; i++) {
            mas[i] = random.nextInt(100) + 1;
            text.append("[").append(i).append("] = ").append(mas[i]).append("; ");
            text.append(" ");
        }

        resultArea.setText(text.append("\nРезультаты потоков\n\n").toString());

        th1 = new Thread1();
        th2 = new Thread2();
        th3 = new Thread3();
        th4 = new Thread4();

        Thread[] threads = {th1, th2, th3, th4};

        for (int i = 0; i < threads.length; i++) {
            threads[i].setPriority(Thread.NORM_PRIORITY);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        coordinator = new Thread(() -> {
            Thread current = Thread.currentThread();
            try {
                th1.join();
                th2.join();
                th3.join();
                th4.join();
            } catch (InterruptedException e) {
                printText("\nКоординатор был прерван.\n");
                e.printStackTrace();
                return;
            }

            if (!current.isInterrupted()) {
                printText("\nВсе четыре потока завершили работу.\n");
                Platform.runLater(() -> startButton.setDisable(false));
            }
        }, "Coordinator");

        coordinator.start();
    }

    class Thread1 extends Thread {
        Thread1() {
            super("Th1");
        }

        @Override
        public void run() {
            sumOddPairs(getName(), 0, 1);
            finishAndPrint(this, th4, "Th1 (Фамилия): Борисенко");
        }
    }

    class Thread2 extends Thread {
        Thread2() {
            super("Th2");
        }

        @Override
        public void run() {
            sumOddPairs(getName(), mas.length - 1, -1);
            finishAndPrint(this, null, "Th2 (Имя): Иван");
        }
    }

    class Thread3 extends Thread {
        Thread3() {
            super("Th3");
        }

        @Override
        public void run() {
            traverseInterval(getName(), 0, 798, 1);
            finishAndPrint(this, th1, "Th3 (Дисциплина): Programarea concurentă și distribuită");
        }
    }

    class Thread4 extends Thread {
        Thread4() {
            super("Th4");
        }

        @Override
        public void run() {
            traverseInterval(getName(), 2111, 1456, -1);
            finishAndPrint(this, th2, "Th4 (Группа): CR-243");
        }
    }

    void sumOddPairs(String name, int start, int step) {
        printText("Запуск " + name + "\n");

        int[] odd = new int[4];
        int count = 0;

        for (int i = start; i >= 0 && i < mas.length; i += step) {
            int number = mas[i];

            if (number % 2 != 0) {
                odd[count] = number;
                count++;
            }

            if (count == 4) {
                int sum1 = odd[0] + odd[1];
                int sum2 = odd[2] + odd[3];

                printText(name + ": (" + odd[0] + " + " + odd[1] + ") + ("
                        + odd[2] + " + " + odd[3] + ") = "
                        + sum1 + " + " + sum2 + " = " + (sum1 + sum2) + "\n");
                count = 0;
            }
        }
    }

    void traverseInterval(String name, int start, int end, int step) {
        printText("Запуск " + name + "\n");

        StringBuilder line = new StringBuilder(name + ": ");
        int count = 0;

        for (int i = start; (step > 0 ? i <= end : i >= end); i += step) {
            line.append(i).append(" ");
            count++;

            if (count == 10 || i == end) {
                printText(line.append("\n").toString());
                line = new StringBuilder(name + ": ");
                count = 0;
            }
        }
    }

    void finishAndPrint(Thread current, Thread waitForThread, String info) {
        current.setPriority(8);

        while (!current.isInterrupted() && (th1.getPriority() < 8 || th2.getPriority() < 8 || th3.getPriority() < 8 || th4.getPriority() < 8)) {
            Thread.yield();
        }

        if (waitForThread != null) {
            while (!current.isInterrupted() && waitForThread.isAlive()) {
                Thread.yield();
            }
        }

        if (current.isInterrupted()) {
            return;
        }

        if (waitForThread == null) {
            printText("\nДанные студента\n");
        }

        for (int i = 0; i < info.length(); i++) {
            printText(String.valueOf(info.charAt(i)));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                printText("\nSleep был прерван.\n");
                e.printStackTrace();
                return;
            }
        }

        if (!current.isInterrupted()) {
            printText("\n");
        }
    }

    private void printText(String text) {
        Platform.runLater(() -> resultArea.appendText(text));
    }

    @Override
    public void stop() {
        Thread[] threads = new Thread[]{coordinator, th1, th2, th3, th4};
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] != null) {
                threads[i].interrupt();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}