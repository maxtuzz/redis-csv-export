package org.redis.exporter;

import org.redis.exporter.processor.Processor;

public class Application {
    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();

        Processor processor = new Processor();
        processor.run();

        final long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");
    }
}