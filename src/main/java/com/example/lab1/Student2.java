package com.example.lab1;

public class Student2 implements Runnable {
    private int[] mas;
    public Student2(int[] mas) {
        this.mas = mas;
    }
    @Override
    public void run() {
        int[] numbers = new int[4];
        int count = 0;
        for (int i = mas.length - 1; i >= 0; i--) {
            if (mas[i] % 2 != 0) {
                numbers[count] = mas[i];
                count++;
                if (count == 4) {

                    int sum1 = numbers[0] + numbers[1];
                    int sum2 = numbers[2] + numbers[3];
                    int sum = sum1 + sum2;

                    Main.printText(Thread.currentThread().getName()
                                    + ": ((" + numbers[0] + " + " + numbers[1]
                                    + ") + (" + numbers[2] + " + " + numbers[3]
                                    + ")) = " + sum + "\n"
                    );
                    count = 0;
                }
            }
        }
    }
}