package com.example.lab1;

public class Student1 implements Runnable {

    private int[] mas;
    private int start;
    private int step;

    public Student1(int[] mas, int start, int step) {
        this.mas = mas;
        this.start = start;
        this.step = step;
    }

    @Override
    public void run() {

        int first = -1;

        for (int i = start; i >= 0 && i < mas.length; i += step) {
            if (mas[i] % 2 != 0) {
                if (first == -1) {
                    first = mas[i];
                } else {
                    int sum = first + mas[i];

                    Main.printText(Thread.currentThread().getName() + ": " + first + " + " + mas[i] + " = " + sum + "\n");

                    first = -1;
                }
            }
        }
    }
}