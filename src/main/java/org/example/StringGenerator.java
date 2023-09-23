package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class StringGenerator {

    // Генерация случайной строки длиной от min до max символов до и после разделителя
    private static String generateRandomString(int min, int max) {
        Random random = new Random();
        int length = random.nextInt(max - min + 1) + min;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char randomChar = (char) (random.nextInt(26) + 'a'); // Генерация случайной буквы в диапазоне 'a' - 'z'
            stringBuilder.append(randomChar);
        }
        return stringBuilder.toString();
    }

    public static void generateToFile(String filePath, int numLines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < numLines; i++) {
                String line = generateRandomString(5, 15) + " : " + generateRandomString(25, 100);
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void main(String[] args) {
        String filePath = "generated_strings.txt";
        int numLines = 150_000_000; // Количество строк генерируемых в файл
        try {
            generateToFile(filePath, numLines);
            System.out.println("Сгенерировано " + numLines + " строк в файл " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}