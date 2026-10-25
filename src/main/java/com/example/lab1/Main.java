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

    private TextArea output;
    private Button mediumButton;
    private Button advancedButton;
    private int messagesReceived;

    @Override
    public void start(Stage stage) {
        Label title = new Label("Лабораторная работа №2. Вариант 3");

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

        // Читаем данные до запуска, пока потоки ещё не завершились.
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

        // Запускаем все потоки выбранного уровня.
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
    }

    private Thread[] createThreads(boolean advanced) {
        // Создаём группы и их вложенность.
        ThreadGroup mainGroup = new ThreadGroup("Main");
        ThreadGroup gn = new ThreadGroup(mainGroup, "GN");
        ThreadGroup gh = new ThreadGroup(gn, "GH");
        ThreadGroup gm = new ThreadGroup(mainGroup, "GM");

        // Каждый вызов создаёт один поток из варианта 3.
        return new Thread[] {
                createThread(gh, "Tha", 4, advanced),
                createThread(gh, "Thb", 3, advanced),
                createThread(gh, "Thc", 6, advanced),
                createThread(gh, "Thd", 3, advanced),

                createThread(gn, "ThA", 3, advanced),

                createThread(gm, "Th1", 2, advanced),
                createThread(gm, "Th2", 3, advanced),
                createThread(gm, "Th3", 3, advanced),

                createThread(mainGroup, "Th1", 8, advanced),
                createThread(mainGroup, "Th2", 3, advanced)
        };
    }

    private Thread createThread(ThreadGroup group, String name, int priority, boolean advanced) {
        if (advanced) {
            Thread thread = new MyThread(group, name, this);
            thread.setPriority(priority);
            return thread;
        } else {
            Thread thread = new Thread(group, name);
            thread.setPriority(priority);
            return thread;
        }
    }

    // Этот метод вызывают потоки продвинутого уровня.
    public void printResult(String text) {
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