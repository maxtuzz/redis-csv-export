package org.redis.exporter.processor;

import com.google.common.collect.Iterables;
import org.redis.exporter.config.JedisConfig;
import redis.clients.jedis.Jedis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Processor {
    private Jedis jedis;

    public Processor() {
        String redisHost = System.getenv("REDIS_HOST");

        if (redisHost == null) {
            redisHost = "localhost:6379";
        }

        jedis = new JedisConfig(redisHost).getInstance();
    }

    public void run() {
        final long startTime = System.currentTimeMillis();
        System.out.println("Fetching keys ...");
        final Set<String> keys = jedis.keys("*.*");

        final long keyFetchEndTime = System.currentTimeMillis();

        System.out.println("Fetched keys in: " + (keyFetchEndTime - startTime) + "ms");
        System.out.println(keys.size() + " keys logged for export ...");

        exportKeyData(keys);
    }


    /**
     * Partition keys into batches and process
     * @param keys set of all keys
     */
    private void exportKeyData(Set<String> keys) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.csv"))) {
            List<String> values = new LinkedList<>();
            values.add("stockOnHand,sku,unavailableSoh,branch,lastUpdateTime");

            // Write first line
            writeToOutput(writer, values);

            Iterables.partition(keys, 1000).forEach(chunk -> {
                List<String> csvOutput = processBatch(chunk);

                writeToOutput(writer, csvOutput);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches hmap data from redis and returns a list of csv formatted lines
     *
     * @param keys list of keys matching import pattern
     * @return CSVOutput
     */
    private List<String> processBatch(List<String> keys) {
        System.out.println("Fetching hmap data from keys ...");

        final long streamStartTime = System.currentTimeMillis();
        List<Map<String, String>> maps = keys.stream()
                .map(jedis::hgetAll)
                .collect(Collectors.toList());

        final long streamEndTime = System.currentTimeMillis();
        System.out.println("Batch stream data retrieved in: " + (streamEndTime - streamStartTime) + "ms");

        System.out.println("Formatting hmap data to CSV...");

        return getCSVOutput(maps);
    }

    /**
     * Outputs list of strings to a csv file
     *
     * @param writer    file writer
     * @param csvOutput output to write
     */
    private void writeToOutput(BufferedWriter writer, List<String> csvOutput) {
        try {
            for (String s : csvOutput) {
                writer.append(s);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns list of formatted lines
     *
     * @param maps redis hash entries
     * @return lines
     */
    private List<String> getCSVOutput(List<Map<String, String>> maps) {
        List<String> values = new LinkedList<>();

        maps.forEach(map -> {
            String stock = map.get("stockOnHand");
            String sku = map.get("sku");
            String unavailableSoh = map.get("unavailableSoh");
            String branch = map.get("branch");
            String lastUpdateTime = map.get("lastUpdateTime");

            final String join = String.join(
                    ",", stock, sku, unavailableSoh, branch, lastUpdateTime
            );

            values.add(join);
        });

        return values;
    }
}
