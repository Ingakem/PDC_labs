package com.example.lab1;

public class MyThread extends Thread {

    Main app;

    public MyThread(ThreadGroup group, String name, Main app) {
        super(group, name);
        this.app = app;
    }

    @Override
    public void run() {
        String text = "Поток: " + getName()
                + " | Группа: " + getThreadGroup().getName()
                + " | Приоритет: " + getPriority();

        // Обязательный вывод в консоль по условию задания.
        System.out.println(text);

        // Дополнительно показываем тот же текст в окне JavaFX.
        app.printResult(text);
    }
}