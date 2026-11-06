package com.example.lab1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.Phaser;

class CustomFX2Thread extends Thread {
    private final Phaser phaser;

    public CustomFX2Thread(ThreadGroup group, String name, int priority, Phaser phaser) {
        super(group, name);
        setPriority(priority);
        this.phaser = phaser;
    }

    @Override
    public void run() {
        String msg = String.format("Run() Поток: %-5s | Группа: %-5s | Приоритет: %d\n",
                getName(), getThreadGroup().getName(), getPriority());

        System.out.print(msg);
        Main2.printLog(msg);

        phaser.awaitAdvance(0);
    }
}

public class Main2 extends Application {
    private static TextArea logArea;

    public static void printLog(String text) {
        Platform.runLater(() -> logArea.appendText(text));
    }

    @Override
    public void start(Stage stage) {
        Button btnMed = new Button("Средний уровень");
        Button btnAdv = new Button("Продвинутый уровень");

        btnMed.setOnAction(e -> execute(false));
        btnAdv.setOnAction(e -> execute(true));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Consolas';");

        VBox root = new VBox(10, new Label("Вариант 4"), new HBox(10, btnMed, btnAdv), logArea);
        root.setPadding(new Insets(10));
        VBox.setVgrow(logArea, Priority.ALWAYS);

        stage.setScene(new Scene(root, 600, 450));
        stage.show();
    }

    private void execute(boolean advanced) {
        logArea.clear();
        logArea.appendText((advanced ? "ПРОДВИНУТЫЙ" : "СРЕДНИЙ") + " УРОВЕНЬ \n");

        ThreadGroup mainG = new ThreadGroup("Main");
        ThreadGroup gz = new ThreadGroup(new ThreadGroup(mainG, "GO"), "GZ");
        ThreadGroup gv = new ThreadGroup(mainG, "GV");
        ThreadGroup gf = new ThreadGroup(mainG, "GF");

        Phaser phaser = new Phaser(1);
        Runnable waitTask = () -> phaser.awaitAdvance(0);

        Thread[] threads = {
                create(gz, "Th_Main_GO_GZa", 1, advanced, waitTask, phaser),
                create(gz, "Th_Main_GO_GZb", 3, advanced, waitTask, phaser),
                create(gz, "Th_Main_GO_GZc", 3, advanced, waitTask, phaser),
                create(gz, "Th_Main_GO_GZd", 7, advanced, waitTask, phaser),
                create(gv, "Th_Main_GVA", 3, advanced, waitTask, phaser),
                create(gf, "Th_Main_GF1", 5, advanced, waitTask, phaser),
                create(gf, "Th_Main_GF2", 3, advanced, waitTask, phaser),
                create(gf, "Th_Main_GF3", 9, advanced, waitTask, phaser),
                create(mainG, "Th_Main1", 3, advanced, waitTask, phaser),
                create(mainG, "Th_Main2", 5, advanced, waitTask, phaser)
        };

        for (Thread t : threads) t.start();

        Thread[] active = new Thread[mainG.activeCount() * 2];
        int count = mainG.enumerate(active, true);

        for (int i = 0; i < count; i++) {
            Thread t = active[i];
            logArea.appendText(String.format("Поток: %-5s | Группа: %-5s | Приоритет: %d\n",
                    t.getName(), t.getThreadGroup().getName(), t.getPriority()));
        }

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream customOut = new PrintStream(baos);
        System.setOut(customOut);

        mainG.list();
        System.out.flush();

        String listOutput = baos.toString();
        logArea.appendText("\nДерево (list)\n" + listOutput);

        phaser.arrive();
    }

    private Thread create(ThreadGroup g, String n, int p, boolean adv, Runnable waitTask, Phaser ph) {
        Thread t = adv ? new CustomFX2Thread(g, n, p, ph) : new Thread(g, waitTask, n);
        if (!adv) t.setPriority(p);
        return t;
    }

    public static void main(String[] args) { launch(args); }
}