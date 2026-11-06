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

public class main extends Application {

    private static TextArea resultArea;
    private Button startButton;
    private static final int[] mas = new int[100];

    private static Thread th1, th2, th3, th4, coordinator;

    private static final String INFO1_PRENUMELE = "\nTh1 (Имя): Dumitru";
    private static final String INFO2_NUMELE = "\nTh2 (Фамилия): Ciolac";
    private static final String INFO3_DISCIPLINA = "\nTh3 (Дисциплина): Programarea concurenta si distribuita";
    private static final String INFO4_GRUPA = "\nTh4 (Группа): CR-243";

    @Override
    public void start(Stage stage) {
        Label label = new Label("Лабораторная работа №3 Вариант 5");
        startButton = new Button("Запустить");
        startButton.setOnAction(e -> startWork());

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        VBox root = new VBox(10, label, startButton, resultArea);
        root.setPadding(new Insets(10));

        stage.setTitle("Лабораторная работа 3 - Вариант 5");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void startWork() {
        startButton.setDisable(true);
        resultArea.setText("Исходный массив (100 элементов):\n");

        Random random = new Random();
        for (int i = 0; i < mas.length; i++) {
            mas[i] = random.nextInt(100) + 1;
            resultArea.appendText("[" + i + "]=" + mas[i] + ((i + 1) % 10 == 0 ? "\n" : " "));
        }
        resultArea.appendText("\nЗапуск потоков\n\n");

        th1 = new Thread1();
        th2 = new Thread2();
        th3 = new Thread3();
        th4 = new Thread4();

        th1.start();
        th2.start();
        th3.start();
        th4.start();

        coordinator = new Thread(() -> {
            while (th1.isAlive() || th2.isAlive() || th3.isAlive() || th4.isAlive()) {
                Thread.yield();
            }
            printText("\n\nВсе четыре потока завершили работу.\n");
            Platform.runLater(() -> startButton.setDisable(false));
        }, "Coordinator");
        coordinator.start();
    }

    static class Thread1 extends Thread {
        Thread1() { super("Th1"); }
        @Override
        public void run() {
            calcEvenPosProducts(this, 0, 2);
            finishAndPrint(this, th4, INFO1_PRENUMELE);
        }
    }

    static class Thread2 extends Thread {
        Thread2() { super("Th2"); }
        @Override
        public void run() {
            calcEvenPosProducts(this, mas.length - 2, -2);
            finishAndPrint(this, null, INFO2_NUMELE);
        }
    }

    static class Thread3 extends Thread {
        Thread3() { super("Th3"); }
        @Override
        public void run() {
            traverseInterval(this, 567, 1002, 1);
            finishAndPrint(this, th1, INFO3_DISCIPLINA);
        }
    }

    static class Thread4 extends Thread {
        Thread4() { super("Th4"); }
        @Override
        public void run() {
            traverseInterval(this, 1100, 567, -1);
            finishAndPrint(this, th2, INFO4_GRUPA);
        }
    }

    private static void calcEvenPosProducts(Thread current, int startIdx, int step) {
        while (!current.isInterrupted()) {
            printText("Starting " + current.getName() + "\n");
            int[] nums = new int[4];
            int[] idx = new int[4];
            int count = 0;

            for (int i = startIdx; i >= 0 && i < mas.length; i += step) {
                nums[count] = mas[i];
                idx[count++] = i;

                if (count == 4) {
                    int prod1 = nums[0] * nums[1];
                    int prod2 = nums[2] * nums[3];
                    printText(current.getName() + " [поз. " + idx[0] + "," + idx[1] + "," + idx[2] + "," + idx[3]
                            + "]: ((" + nums[0] + " * " + nums[1] + ") + (" + nums[2] + " * " + nums[3]
                            + ")) = " + prod1 + " + " + prod2 + " = " + (prod1 + prod2) + "\n");
                    count = 0;
                    pauseMs(60);
                }
            }
            current.interrupt();
        }
    }

    private static void traverseInterval(Thread current, int start, int end, int step) {
        printText("Starting " + current.getName() + "\n");
        StringBuilder line = new StringBuilder(current.getName() + ": ");
        int count = 0;

        for (int i = start; (step > 0 ? i <= end : i >= end); i += step) {
            line.append(i).append(" ");
            if (++count == 10 || i == end) {
                printText(line.append("\n").toString());
                line.setLength(0);
                if (i != end) line.append(current.getName()).append(": ");
                count = 0;
                Thread.yield();
                pauseMs(15);
            }
        }
    }

    private static void finishAndPrint(Thread current, Thread waitForThread, String info) {
        current.setPriority(8);
        while (th1.getPriority() < 8 || th2.getPriority() < 8
                || th3.getPriority() < 8 || th4.getPriority() < 8) {
            Thread.yield();
        }
        if (waitForThread != null) {
            while (waitForThread.isAlive()) {
                Thread.yield();
            }
        }
        for (int i = 0; i < info.length(); i++) {
            printText(String.valueOf(info.charAt(i)));
            pauseMs(100);
        }
        current.setPriority(9);
    }

    private static void pauseMs(long millis) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < millis) {
            Thread.yield();
        }
    }

    public static void printText(String text) {
        Platform.runLater(() -> resultArea.appendText(text));
    }

    @Override
    public void stop() {
        if (th1 != null) th1.interrupt();
        if (th2 != null) th2.interrupt();
        if (th3 != null) th3.interrupt();
        if (th4 != null) th4.interrupt();
        if (coordinator != null) coordinator.interrupt();
    }

    public static void main(String[] args) {
        launch(args);
    }
}