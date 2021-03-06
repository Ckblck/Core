package net.oldust.sync.jedis;

import lombok.Getter;
import net.oldust.core.Core;
import org.apache.commons.io.FileUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.yaml.snakeyaml.Yaml;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JedisManager {
    @Getter
    private static JedisManager instance;
    @Getter
    private JedisPool pool;

    private final File credentialsFile;

    public JedisManager() {
        this(new File(Core.getInstance().getDataFolder(), "redis_credentials.yml"));
    }

    public JedisManager(File credentialsFile) {
        this.credentialsFile = credentialsFile;

        if (!credentialsFile.exists())
            createFile();

        Yaml yaml = new Yaml();

        try (FileInputStream inputStream = new FileInputStream(credentialsFile)) {
            Map<String, Object> map = yaml.load(inputStream);

            String hostName = (String) map.get("host-name");
            int port = (int) map.get("port");

            GenericObjectPoolConfig<?> jedisPoolConfig = new GenericObjectPoolConfig<>();

            jedisPoolConfig.setMaxTotal(30);
            jedisPoolConfig.setMaxIdle(0);
            jedisPoolConfig.setMinIdle(12);
            jedisPoolConfig.setBlockWhenExhausted(true);
            jedisPoolConfig.setMaxWaitMillis(5000);
            jedisPoolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(6));

            pool = new JedisPool(jedisPoolConfig, hostName, port, 5000);
            instance = this;
        } catch (IOException e) {
            e.printStackTrace();

            System.exit(1);
        }

    }

    private void createFile() {
        File file = new File(credentialsFile.toURI());

        try {
            InputStream resource = Core.class.getResourceAsStream("/redis_credentials.yml");

            FileUtils.copyInputStreamToFile(resource, credentialsFile);
            Files.copy(file.toPath(), credentialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("The Redis credentials file was created. Fill it correctly and start the server again.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

}
