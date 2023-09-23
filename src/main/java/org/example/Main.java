package org.example;

import java.io.File;
import java.io.IOException;

public class Main {


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String inputFile = "generated_strings.txt";
        String outputFile = "result.txt";

        try {
            ExternalSorter sorter = new ExternalSorter();
            sorter.sortLargeFile(inputFile, outputFile);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        File generatedStrings = new File(inputFile);
        File result = new File(outputFile);
        System.out.println(generatedStrings.length());
        System.out.println(result.length());
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime + "< Elapsed time in milliseconds");
        }
    }

