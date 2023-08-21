package nju.lab.DSchecker.util.monitor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class PerformanceMonitor {
    private static PrintWriter writer;
    private static long startTime;

    private static HashMap<String, Long> startTimes = new HashMap<>();
    private static long endTime;
    public static void initialize(String filename) {
        try {
            writer = new PrintWriter(new FileWriter(filename, false));
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<performanceData>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void start() {
        // Record the start time
        startTime = System.nanoTime();
    }
    public static void start(String functionName) {
        // Record the start time
        startTimes.put(functionName, System.nanoTime());
    }
    public static void stop(String functionName) {
        endTime = System.nanoTime();
        long elapsedTime = endTime - startTimes.get(functionName);
        writer.println("    <function>");
        writer.println("        <name>" + functionName + "</name>");
        writer.println("        <runtime>" + (double) elapsedTime / 1000000000 + "</runtime>");
        writer.println("    </function>");
        writer.flush();
    }

    public static void close() {
        if (writer != null) {
            writer.println("</performanceData>");
            writer.close();
        }
    }
}
