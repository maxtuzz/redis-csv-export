package org.redis.exporter;

import org.redis.exporter.config.JedisConfig;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class Importer {
    public static void main(String[] args) {
        Jedis jedis = new JedisConfig("localhost:6379").getInstance();

        Map<String, String> map = new HashMap<>();
        map.put("stockOnHand", "1230");
        map.put("sku", "1230");
        map.put("unavailableSoh", "1230");
        map.put("branch", "127");
        map.put("lastUpdateTime", "127-123:123");

        final int recordCount = 15000;

        System.out.println("Importing " + recordCount + " records...");
        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < recordCount; i++) {
            jedis.hmset(i + ".123", map);
        }

        final long endTime = System.currentTimeMillis();

        System.out.println("Total execution time: " + (endTime - startTime) + "ms");
    }
}
