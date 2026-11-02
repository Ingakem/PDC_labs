package com.example.lab1;

public class MyThread extends Thread {

    public MyThread(ThreadGroup group, String name) {
        super(group, name);
    }

    @Override
    public void run() {
        String text = "Поток: " + getName()
                + " | Группа: " + getThreadGroup().getName()
                + " | Приоритет: " + getPriority();

        System.out.println(text);

        Main.printResult(text);
    }
}