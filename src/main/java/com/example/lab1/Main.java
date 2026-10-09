package com.example.lab1;

import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private static TextArea resultArea;
    private Button startButton;

    @Override
    public void start(Stage stage) {
        Label label = new Label("Вариант 3. Суммы нечётных чисел по два");

        startButton = new Button("Запустить");

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);

        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                startWork();
            }
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(label, startButton, resultArea);

        VBox.setVgrow(resultArea, Priority.ALWAYS);

        Scene scene = new Scene(root, 750, 550);

        stage.setTitle("Лабораторная работа 1 — Java");
        stage.setScene(scene);
        stage.show();
    }

    private void startWork() {
        startButton.setDisable(true);
        resultArea.setText("Массив:\n");

        int[] mas = new int[100];
        Random random = new Random();

        for (int i = 0; i < mas.length; i++) {
            mas[i] = random.nextInt(100) + 1;
            resultArea.appendText(mas[i] + " ");
        }

        resultArea.appendText("\n\n");

        Thread th1 = new Thread(new Student1(mas, 0, 1), "Иван, Th1");
        Thread th2 = new Thread(new Student1(mas, 0, 1), "Иван, Th2");

        Thread th3 = new Thread(new Student2(mas, false), "Дима, Th1");
        Thread th4 = new Thread(new Student2(mas, false), "Дима, Th2");

        th1.start();
        th2.start();
        th3.start();
        th4.start();

        Thread waitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    th1.join();
                    th2.join();
                    th3.join();
                    th4.join();

                    printText("\nВсе потоки завершили работу.\n");
                    printText("\nИнформация о студентах:\n");

                    String info = "Borisenco Ivan, CR-243\n" + "Ciolac Dumitru, CR-243";

                    for (int i = 0; i < info.length(); i++) {
                        printText(String.valueOf(info.charAt(i)));
                        Thread.sleep(100);
                    }
                    printText("\n");
                } catch (InterruptedException e) {
                    printText("\nОжидание было прервано.\n");
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setDisable(false);
                        }
                    });
                }
            }
        });
        waitThread.setDaemon(true);
        waitThread.start();
    }

    public static void printText(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                resultArea.appendText(text);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}