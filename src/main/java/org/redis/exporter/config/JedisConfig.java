package org.redis.exporter.config;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

public class JedisConfig {
    private Jedis jedis;

    public JedisConfig(String hostname) {
        final String[] args = hostname.split(":");

        String host = args[0];
        int port;

        try {
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Using default redis port");
            port = 6379;
        }

        jedis = new Jedis(new HostAndPort(host, port));
    }

    public Jedis getInstance() {
        return jedis;
    }
}
