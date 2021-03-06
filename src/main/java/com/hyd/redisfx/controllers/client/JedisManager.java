package com.hyd.redisfx.controllers.client;

import com.hyd.redisfx.conn.Connection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Consumer;

/**
 * @author yiding.he
 */
public class JedisManager {

    private static JedisPool jedisPool;

    private static Connection connection;

    private static int currentDatabase;

    public static void setCurrentDatabase(int currentDatabase) {
        JedisManager.currentDatabase = currentDatabase;
    }

    public static int getCurrentDatabase() {
        return currentDatabase;
    }

    public static void withJedis(Consumer<Jedis> operation) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(currentDatabase);
            operation.accept(jedis);
        }
    }

    public static Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        jedis.select(currentDatabase);
        return jedis;
    }

    public static void connect(String host, int port) {
        connect(host, port, null);
    }

    public static void connect(String host, int port, String passphase) {
        Connection connection = new Connection();
        connection.setHost(host);
        connection.setPort(port);
        connection.setPassphase(passphase);
        connect(connection);
    }

    public static void connect(Connection connection) {
        JedisPool jedisPool = createJedisPool(connection);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.get("name");
        }

        // Assign values after successfully connected.
        JedisManager.jedisPool = jedisPool;
        JedisManager.connection = connection;
    }

    public static String getHost() {
        return connection == null? null: connection.getHost();
    }

    public static int getPort() {
        return connection == null ? 0 : connection.getPort();
    }

    public static JedisPool createJedisPool(Connection connection) {
        return createJedisPool(connection.getHost(), connection.getPort(), connection.getPassphase());
    }

    public static JedisPool createJedisPool(String host, Integer port, String passphase) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

        if (!StringUtils.isBlank(passphase)) {
            return new JedisPool(poolConfig, host, port, 10000, passphase);
        } else {
            return new JedisPool(poolConfig, host, port, 10000);
        }
    }
}
