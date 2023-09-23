package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExternalSorter {

    // Указываем, что занимать память мы можем только на 1/8 в процессе чтения большого файла.
    private static final long maxRuntimeMemory = Runtime.getRuntime().maxMemory();
    private static final long reservedMemory = maxRuntimeMemory / 10;

    public ExternalSorter() {
    }

    public void sortLargeFile(String inputFilePath, String outputFilePath) throws IOException, InterruptedException {

        // Разбиение и сортировка временных файлов
        splitAndSort(inputFilePath);

        // Merge всех временных файлов в один
        mergeSortedChunks(outputFilePath);
    }

    private void writeChunkToFile(List<String> chunk, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            for (String line : chunk) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        }
    }

    private void splitAndSort(String inputFilePath) throws IOException, InterruptedException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFilePath))) {
            List<String> chunk = new ArrayList<>();
            String line;
            int fileNumber = 0;

            // цикл ниже обеспечивает максимальную утилизацию памяти, выделенной для JVM
            long sortingThreshold = maxRuntimeMemory / 10 * 8;  //
            while ((line = reader.readLine()) != null) {
                chunk.add(line);
                if (Runtime.getRuntime().freeMemory() < reservedMemory && // свободной памяти меньше 10% от максимума?
                Runtime.getRuntime().totalMemory() > sortingThreshold) {  // хип меньше, чем 80% от памяти выделенной JVM? чтобы хип увеличивался и временные файлы были как можно больше
                    Collections.sort(chunk);
                    writeChunkToFile(chunk, "temp_chunk_" + fileNumber + ".txt");
                    chunk.clear();
                    fileNumber++;
                    Thread.sleep(10);
                    System.gc();            // без явного вызова GC хип освобождается некорректно в таком сценарии.
                    Thread.sleep(10);   // Происходит постоянный вход в этот цикл т.к. память занята и плодятся 1кб временные файлы бесконечно.

                }
            }

            if (!chunk.isEmpty()) {
                Collections.sort(chunk);
                writeChunkToFile(chunk, "temp_chunk_" + fileNumber + ".txt");
            }
        }
    }


    private void mergeSortedChunks(String outputFilePath) throws IOException {
        List<BufferedReader> readers = new ArrayList<>();

        // лист указателей на строки в каждом временном файле для их сравнения между собой
        // и записи самой "меньшей" строки.
        List<String> currentLines = new ArrayList<>();

        // Открываем ридеры для всех временных файлов и читаем первую строку из каждого файла
        for (File file : new File(".").listFiles((dir, name) -> name.startsWith("temp_chunk_"))) {
            BufferedReader reader = Files.newBufferedReader(file.toPath());
            readers.add(reader);
            String firstLine = reader.readLine();
            currentLines.add(firstLine);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath))) {

            while (!currentLines.isEmpty()) {
                String minLine = Collections.min(currentLines, Comparator.comparing(
                        s -> {
                            int dividerPosition = s.indexOf(':');
                            if (dividerPosition >= 0) {
                                return s.substring(0, dividerPosition);
                            } else {
                                System.out.println(s);
                                return s;
                            }
                        })
                );

                int minIndex = currentLines.indexOf(minLine);
                writer.write(minLine);
                writer.newLine();

                // Читаем следующую строку из файла, из которого была выбрана минимальная строка
                BufferedReader minReader = readers.get(minIndex);
                String nextLine = minReader.readLine();
                if (nextLine != null) {
                    currentLines.set(minIndex, nextLine);
                } else {
                    // Если достигнут конец файла, закрываем ридер и удаляем указатель
                    minReader.close();
                    currentLines.remove(minIndex);
                    readers.remove(minReader);
                }
            }

        }
        // Закрываем оставшиеся ридеры
        for (BufferedReader reader : readers) {
            reader.close();
        }
        // Удаляем временные файлы
        for (File file : new File(".").listFiles((dir, name) -> name.startsWith("temp_chunk_"))) {
            file.delete();
        }
    }

}