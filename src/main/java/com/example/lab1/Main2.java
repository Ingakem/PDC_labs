package com.example.lab1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.Phaser;

class CustomFXThread extends Thread {
    private final TextArea logArea;
    private final Phaser phaser;

    public CustomFXThread(ThreadGroup group, String name, int priority, TextArea logArea, Phaser phaser) {
        super(group, name);
        setPriority(priority);
        this.logArea = logArea;
        this.phaser = phaser;
    }

    @Override
    public void run() {
        String groupName = (getThreadGroup() != null) ? getThreadGroup().getName() : "N/A";
        String msg = String.format("Поток: %-5s | Группа: %-5s | Приоритет: %d%n",
                getName(), groupName, getPriority());

        System.out.print(msg);
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(msg));
        }

        phaser.awaitAdvance(0);
    }
}

public class Main2 extends Application {

    public static class ThreadInfo {
        private final SimpleIntegerProperty index;
        private final SimpleStringProperty name;
        private final SimpleStringProperty group;
        private final SimpleIntegerProperty priority;
        private final SimpleStringProperty state;

        public ThreadInfo(int index, String name, String group, int priority, String state) {
            this.index = new SimpleIntegerProperty(index);
            this.name = new SimpleStringProperty(name);
            this.group = new SimpleStringProperty(group);
            this.priority = new SimpleIntegerProperty(priority);
            this.state = new SimpleStringProperty(state);
        }

        public int getIndex() { return index.get(); }
        public String getName() { return name.get(); }
        public String getGroup() { return group.get(); }
        public int getPriority() { return priority.get(); }
        public String getState() { return state.get(); }
    }

    private TreeView<String> groupTree;
    private TableView<ThreadInfo> threadTable;
    private final ObservableList<ThreadInfo> tableData = FXCollections.observableArrayList();
    private TextArea logArea;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Лабораторная работа №2 - Вариант 4 (JavaFX)");

        Label formulaLabel = new Label(
                "Вариант 4: Main {GO {GZ{Tha(1), Thb(3), Thc(3), Thd(7)}}, GV {ThA(3)}, GF {Th1(5), Th2(3), Th3(9)}, Th1(3), Th2()}"
        );
        formulaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Button btnMedium = new Button("1. Средний уровень (без run())");
        btnMedium.setOnAction(e -> runMediumLevel());

        Button btnAdvanced = new Button("2. Продвинутый уровень (с run())");
        btnAdvanced.setOnAction(e -> runAdvancedLevel());

        HBox buttonBar = new HBox(10, btnMedium, btnAdvanced);
        VBox topBox = new VBox(8, formulaLabel, buttonBar);
        topBox.setPadding(new Insets(10));

        groupTree = new TreeView<>(new TreeItem<>("Нажмите кнопку запуска сверху"));
        groupTree.setPrefWidth(280);

        threadTable = new TableView<>(tableData);
        setupTableColumns();

        SplitPane splitPane = new SplitPane(groupTree, threadTable);
        splitPane.setDividerPositions(0.35);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(160);
        logArea.setStyle("-fx-font-family: 'Consolas', 'Monospaced'; -fx-font-size: 12px;");

        VBox bottomBox = new VBox(5, new Label("Вывод потоков и перечисление:"), logArea);
        bottomBox.setPadding(new Insets(10));
        VBox.setVgrow(logArea, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(splitPane);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 880, 560);
        stage.setScene(scene);
        stage.show();
    }

    private void setupTableColumns() {
        TableColumn<ThreadInfo, Number> colIndex = new TableColumn<>("№");
        colIndex.setCellValueFactory(data -> data.getValue().index);
        colIndex.setPrefWidth(45);

        TableColumn<ThreadInfo, String> colName = new TableColumn<>("Имя потока");
        colName.setCellValueFactory(data -> data.getValue().name);
        colName.setPrefWidth(120);

        TableColumn<ThreadInfo, String> colGroup = new TableColumn<>("Группа");
        colGroup.setCellValueFactory(data -> data.getValue().group);
        colGroup.setPrefWidth(120);

        TableColumn<ThreadInfo, Number> colPriority = new TableColumn<>("Приоритет");
        colPriority.setCellValueFactory(data -> data.getValue().priority);
        colPriority.setPrefWidth(100);

        TableColumn<ThreadInfo, String> colState = new TableColumn<>("Состояние");
        colState.setCellValueFactory(data -> data.getValue().state);
        colState.setPrefWidth(130);

        threadTable.getColumns().addAll(colIndex, colName, colGroup, colPriority, colState);
    }

     //ЗАДАЧА 1: Средний уровень (без переопределения метода run() класса Thread).
    private void runMediumLevel() {
        tableData.clear();
        logArea.clear();
        logArea.appendText(" ЗАПУСК: СРЕДНИЙ УРОВЕНЬ (без переопределения run()) \n");

        ThreadGroup mainGroup = new ThreadGroup("Main");
        ThreadGroup groupGO = new ThreadGroup(mainGroup, "GO");
        ThreadGroup groupGZ = new ThreadGroup(groupGO, "GZ");
        ThreadGroup groupGV = new ThreadGroup(mainGroup, "GV");
        ThreadGroup groupGF = new ThreadGroup(mainGroup, "GF");

        Phaser phaser = new Phaser(1);

        Runnable waitTask = () -> phaser.awaitAdvance(0);

        Thread[] threads = {
                createStandardThread(groupGZ, waitTask, "Tha", 1),
                createStandardThread(groupGZ, waitTask, "Thb", 3),
                createStandardThread(groupGZ, waitTask, "Thc", 3),
                createStandardThread(groupGZ, waitTask, "Thd", 7),
                createStandardThread(groupGV, waitTask, "ThA", 3),
                createStandardThread(groupGF, waitTask, "Th1", 5),
                createStandardThread(groupGF, waitTask, "Th2", 3),
                createStandardThread(groupGF, waitTask, "Th3", 9),
                createStandardThread(mainGroup, waitTask, "Th1", 3),
                createStandardThread(mainGroup, waitTask, "Th2", Thread.NORM_PRIORITY)
        };

        for (Thread t : threads) {
            t.start();
        }

        populateUIAndConsole(mainGroup);

        phaser.arrive();
    }

     //ЗАДАЧА 2: Продвинутый уровень (с переопределением метода run()).
    private void runAdvancedLevel() {
        tableData.clear();
        logArea.clear();
        logArea.appendText(" ЗАПУСК: ПРОДВИНУТЫЙ УРОВЕНЬ (с переопределением run()) \n");

        ThreadGroup mainGroup = new ThreadGroup("Main");
        ThreadGroup groupGO = new ThreadGroup(mainGroup, "GO");
        ThreadGroup groupGZ = new ThreadGroup(groupGO, "GZ");
        ThreadGroup groupGV = new ThreadGroup(mainGroup, "GV");
        ThreadGroup groupGF = new ThreadGroup(mainGroup, "GF");

        Phaser phaser = new Phaser(1);

        CustomFXThread[] threads = {
                new CustomFXThread(groupGZ, "Tha", 1, logArea, phaser),
                new CustomFXThread(groupGZ, "Thb", 3, logArea, phaser),
                new CustomFXThread(groupGZ, "Thc", 3, logArea, phaser),
                new CustomFXThread(groupGZ, "Thd", 7, logArea, phaser),
                new CustomFXThread(groupGV, "ThA", 3, logArea, phaser),
                new CustomFXThread(groupGF, "Th1", 5, logArea, phaser),
                new CustomFXThread(groupGF, "Th2", 3, logArea, phaser),
                new CustomFXThread(groupGF, "Th3", 9, logArea, phaser),
                new CustomFXThread(mainGroup, "Th1", 3, logArea, phaser),
                new CustomFXThread(mainGroup, "Th2", Thread.NORM_PRIORITY, logArea, phaser)
        };

        for (CustomFXThread t : threads) {
            t.start();
        }

        populateUIAndConsole(mainGroup);

        phaser.arrive();
    }

    private Thread createStandardThread(ThreadGroup group, Runnable target, String name, int priority) {
        Thread thread = new Thread(group, target, name);
        thread.setPriority(priority);
        return thread;
    }

    private void populateUIAndConsole(ThreadGroup rootGroup) {
        TreeItem<String> rootItem = buildTreeItem(rootGroup);
        groupTree.setRoot(rootItem);

        Thread[] activeThreads = new Thread[rootGroup.activeCount() * 2 + 10];
        int count = rootGroup.enumerate(activeThreads, true);

        System.out.println("\n Перечисление потоков (Вариант 4) ");
        for (int i = 0; i < count; i++) {
            Thread t = activeThreads[i];
            String gName = (t.getThreadGroup() != null) ? t.getThreadGroup().getName() : "N/A";
            tableData.add(new ThreadInfo(i + 1, t.getName(), gName, t.getPriority(), t.getState().toString()));

            String line = String.format("Поток #%d = %-5s | Группа: %-5s | Приоритет: %d%n",
                    i + 1, t.getName(), gName, t.getPriority());
            System.out.print(line);
            logArea.appendText(line);
        }
    }

    private TreeItem<String> buildTreeItem(ThreadGroup group) {
        TreeItem<String> item = new TreeItem<>("Группа: " + group.getName());
        item.setExpanded(true);

        Thread[] threads = new Thread[group.activeCount() + 5];
        int tCount = group.enumerate(threads, false);
        for (int i = 0; i < tCount; i++) {
            item.getChildren().add(new TreeItem<>(
                    "Поток: " + threads[i].getName() + " (приор. " + threads[i].getPriority() + ")"
            ));
        }

        ThreadGroup[] subGroups = new ThreadGroup[group.activeGroupCount() + 5];
        int gCount = group.enumerate(subGroups, false);
        for (int i = 0; i < gCount; i++) {
            item.getChildren().add(buildTreeItem(subGroups[i]));
        }

        return item;
    }

    public static void main(String[] args) {
        launch(args);
    }
}