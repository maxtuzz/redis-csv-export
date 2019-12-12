package org.redis.exporter;

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

public class Application {
    public static void main(String[] args) {
        Jedis jedis = new JedisConfig("localhost:6379").getInstance();

        final Set<String> keys = jedis.keys("*.*");

        List<Map<String, String>> maps = keys.stream()
                .map(jedis::hgetAll)
                .collect(Collectors.toList());

        maps.forEach(System.out::println);

        List<String> csvOutput = getCSVOutput(maps);

        csvOutput.forEach(x -> {
            if (x.split(",").length < 3) {
                System.err.println("Not enough elements");
            }

            System.out.println(x);
        });

        try {
            writeToFile(csvOutput);
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
    private static List<String> getCSVOutput(List<Map<String, String>> maps) {
        List<String> values = new LinkedList<>();
        values.add("stockOnHand,sku,unavailableSoh,branch,lastUpdateTime");

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

    /**
     * Outputs list of strings to a csv file
     * @param csvOutput list of comma separated values
     * @throws IOException directory access etc.
     */
    private static void writeToFile(List<String> csvOutput) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.csv"))) {
            for (String s : csvOutput) {
                writer.write(s);
                writer.newLine();
            }
        }
    }
}