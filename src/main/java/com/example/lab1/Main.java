package com.example.lab1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private static TextArea output;
    private static Button mediumButton;
    private static Button advancedButton;
    private static int messagesReceived;

    @Override
    public void start(Stage stage) {
        Label title = new Label("Вариант 3");

        mediumButton = new Button("Средний уровень");
        advancedButton = new Button("Продвинутый уровень");

        output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(22);
        output.setText("Выберите уровень выполнения.");

        mediumButton.setOnAction(event -> execute(false));
        advancedButton.setOnAction(event -> execute(true));

        HBox buttons = new HBox(10, mediumButton, advancedButton);
        VBox root = new VBox(10, title, buttons, output);
        root.setPadding(new Insets(10));

        stage.setTitle("Java. Лабораторная работа №2");
        stage.setScene(new Scene(root, 720, 540));
        stage.show();
    }

    private void execute(boolean advanced) {
        output.clear();
        messagesReceived = 0;

        Thread[] threads = createThreads(advanced);

        if (advanced) {
            output.appendText("Продвинутый уровень\n\n");
        } else {
            output.appendText("Средний уровень\n\n");
        }

        output.appendText("Перечень всех 10 потоков до запуска:\n");

        for (int i = 0; i < threads.length; i++) {
            String text = "Поток: " + threads[i].getName()
                    + " | Группа: " + threads[i].getThreadGroup().getName()
                    + " | Приоритет: " + threads[i].getPriority();

            output.appendText(text + "\n");
        }

        if (advanced) {
            output.appendText("\nСообщения самих потоков из run():\n");
            mediumButton.setDisable(true);
            advancedButton.setDisable(true);
        } else {
            output.appendText("\nМетод run() класса Thread не переопределён.\n");
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
    }

    //Main{ GN{ GH{Tha(4), Thb(3), Thc(6), Thd(3)}, ThA(3) },GM{Th1(2), Th2(3), Th3(3)},Th1(8), Th2(3)}
    private Thread[] createThreads(boolean advanced) {
        ThreadGroup mainGroup = new ThreadGroup("Main");
        ThreadGroup gn = new ThreadGroup(mainGroup, "GN");
        ThreadGroup gh = new ThreadGroup(gn, "GH");
        ThreadGroup gm = new ThreadGroup(mainGroup, "GM");

        return new Thread[] {
                createThread(gh, "Tha", Thread.MAX_PRIORITY - 6, advanced), // 4
                createThread(gh, "Thb", Thread.MAX_PRIORITY - 7, advanced), // 3
                createThread(gh, "Thc", Thread.MAX_PRIORITY - 4, advanced), // 6
                createThread(gh, "Thd", Thread.MAX_PRIORITY - 7, advanced), // 3

                createThread(gn, "ThA", Thread.MAX_PRIORITY - 7, advanced), // 3

                createThread(gm, "Th1", Thread.MAX_PRIORITY - 8, advanced), // 2
                createThread(gm, "Th2", Thread.MAX_PRIORITY - 7, advanced), // 3
                createThread(gm, "Th3", Thread.MAX_PRIORITY - 7, advanced), // 3

                createThread(mainGroup, "Th1", Thread.MAX_PRIORITY - 2, advanced), // 8
                createThread(mainGroup, "Th2", Thread.MAX_PRIORITY - 7, advanced)  // 3
        };
    }

    private Thread createThread(ThreadGroup group, String name, int priority, boolean advanced) {
        if (advanced) {
            Thread thread = new MyThread(group, name);
            thread.setPriority(priority);
            return thread;
        } else {
            Thread thread = new Thread(group, name);
            thread.setPriority(priority);
            return thread;
        }
    }

    public static void printResult(String text) {
        Platform.runLater(() -> {
            output.appendText(text + "\n");
            messagesReceived++;

            if (messagesReceived == 10) {
                output.appendText("\nВсе 10 потоков вывели информацию.\n");
                mediumButton.setDisable(false);
                advancedButton.setDisable(false);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}