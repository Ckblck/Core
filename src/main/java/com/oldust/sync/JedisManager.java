package com.oldust.sync;

import lombok.Getter;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

public class JedisManager {
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final String PASSWORD = "pAaSsSwoOoOrDd"; // TODO Change this in production.

    @Getter
    private final JedisPool pool;

    public JedisManager() {
        GenericObjectPoolConfig<?> jedisPoolConfig = new GenericObjectPoolConfig<>();
        jedisPoolConfig.setMaxTotal(30);
        jedisPoolConfig.setMaxIdle(30);
        jedisPoolConfig.setMinIdle(12);
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setMaxWaitMillis(5000);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(6));

        pool = new JedisPool(jedisPoolConfig, HOST, PORT, 5000, PASSWORD);
    }

}
