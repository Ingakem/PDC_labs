package com.example.lab1;

public class Student2 implements Runnable {

    private int[] mas;
    private boolean fromStart;

    public Student2(int[] mas, boolean fromStart) {
        this.mas = mas;
        this.fromStart = fromStart;
    }

    @Override
    public void run() {
        int first = -1;
        if (fromStart) {
            for (int i = 0; i < mas.length; i++) {
                if (mas[i] % 2 != 0) {
                    if (first == -1) {
                        first = mas[i];
                    } else {
                        int sum = first + mas[i];
                        Main.printText(
                                Thread.currentThread().getName() + ": " + first + " + " + mas[i] + " = " + sum + "\n"
                        );
                        first = -1;
                    }
                }
            }
        } else {
            for (int i = mas.length - 1; i >= 0; i--) {
                if (mas[i] % 2 != 0) {
                    if (first == -1) {
                        first = mas[i];
                    } else {
                        int sum = first + mas[i];

                        Main.printText(
                                Thread.currentThread().getName() + ": " + first + " + " + mas[i] + " = " + sum + "\n"
                        );

                        first = -1;
                    }
                }
            }
        }
    }
}