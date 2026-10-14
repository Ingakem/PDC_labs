package com.example.lab1;

public class Student1 implements Runnable {
    private int[] mas;

    public Student1(int[] mas) {
        this.mas = mas;
    }

    @Override
    public void run() {
        int[] numbers = new int[4];
        int count = 0;
        for (int i = 0; i < mas.length; i++) {
            if (mas[i] % 2 != 0) {
                // Запоминаем очередное нечётное число
                numbers[count] = mas[i];
                count++;
                // Собрали четыре числа — складываем две пары
                if (count == 4) {
                    int sum1 = numbers[0] + numbers[1];
                    int sum2 = numbers[2] + numbers[3];
                    int sum = sum1 + sum2;
                    Main.printText(Thread.currentThread().getName()
                                    + ": ((" + numbers[0] + " + " + numbers[1]
                                    + ") + (" + numbers[2] + " + " + numbers[3]
                                    + ")) = " + sum + "\n"
                    );
                    // Начинаем новую группу
                    count = 0;
                }
            }
        }
    }
}