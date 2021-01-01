package net.oldust.core.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.oldust.core.Core;
import net.oldust.core.pool.ThreadPool;
import net.oldust.core.utils.CUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.yaml.snakeyaml.Yaml;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MySQLManager {
    private static HikariDataSource pool;

    private final File credentialsFile;
    private boolean fallback = false;

    public MySQLManager() {
        this(new File(Core.getInstance().getDataFolder(), "db_credentials.yml"));
    }

    public MySQLManager(File credentialsFile) {
        this.credentialsFile = credentialsFile;

        if (!credentialsFile.exists())
            createFile();

        HikariConfig config = new HikariConfig();
        Yaml yaml = new Yaml();

        try (FileInputStream inputStream = new FileInputStream(credentialsFile)) {
            Map<String, Object> map = yaml.load(inputStream);

            String hostName = (String) map.get("host-name");
            String password = (String) map.get("password");
            String username = (String) map.get("username");
            int port = (int) map.get("port");

            config.setJdbcUrl("jdbc:mysql://" + hostName + ":" + port + "/?autoReconnect=true&allowMultiQueries=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true");
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setUsername(username);
            config.setPassword(password);
            config.setConnectionTimeout(7000);
            config.addDataSourceProperty("cachePrepStmts", true);

            pool = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();

            System.exit(1);
        }
    }

    /**
     * Realizar una query de forma asíncrona.
     * Siempre acompañar el future con {@link CompletableFuture#exceptionally(Function)}.
     * Obviar esto resultará en excepciones no avisadas debido al comportamiento natural de un CompletableFuture.
     * {@code      CompletableFuture<CachedRowSet> future = new CompletableFuture<>();
     * future.thenAccept(set -> { ... })
     * .exceptionally(ex -> {
     * ex.printStackTrace();
     * <p>
     * return null;
     * });}
     */

    public static void queryAsync(String statement, CompletableFuture<CachedRowSet> future, Object... placeholders) {
        ThreadPool.getInstance().execute(() -> future.complete(query(statement, placeholders)));
    }

    public static CachedRowSet query(String statement, Object... placeholders) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            for (int i = 0; i < placeholders.length; i++) {
                ps.setObject(i + 1, placeholders[i]);
            }

            CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
            cachedRowSet.populate(ps.executeQuery());

            return cachedRowSet;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Optional<CachedRowSet> queryOptional(String statement, Object... placeholders) {
        return Optional.ofNullable(query(statement, placeholders));
    }

    /**
     * Genera una update y devuelve un {@link CachedRowSet}
     * conteniendo en el index 1 la ID autoincrementada
     * insertada al dar el update.
     * {@code CachedRowSet#getInt(1)} o {@code CachedRowSet#getLong(1)} (si la ID es long)
     */

    public static CachedRowSet updateWithGeneratedKeys(String statement, Object... placeholders) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < placeholders.length; i++) {
                ps.setObject(i + 1, placeholders[i]);
            }

            ps.executeUpdate();

            CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
            cachedRowSet.populate(ps.getGeneratedKeys());

            return cachedRowSet;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int updateThrowable(String statement, Object... placeholders) throws SQLException {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            for (int i = 0; i < placeholders.length; i++) {
                ps.setObject(i + 1, placeholders[i]);
            }

            return ps.executeUpdate();
        }
    }

    public static void updateAsync(String statement, Object... placeholders) {
        ThreadPool.getInstance().execute(() -> update(statement, placeholders));
    }

    public static int update(String statement, Object... placeholders) {
        try {
            return updateThrowable(statement, placeholders);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private void createFile() {
        File file = new File(credentialsFile.toURI());

        try {
            FileUtils.copyInputStreamToFile(Objects.requireNonNull(Core.getInstance().getResource("db_credentials.yml")), credentialsFile);
            Files.copy(file.toPath(), credentialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Se ha creado el archivo de credenciales para la base de datos por primera vez. Rellénalo y reinicia el servidor.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public void validateAddress() {
        Server server = Core.getInstance().getServer();

        String address = getPublicIp("http://checkip.amazonaws.com");
        int port = server.getPort();

        if (address == null) {
            CUtils.inform("Core", "x------------------------------x");
            CUtils.inform("Core", "Could not get public IP address.");
            CUtils.inform("Core", "x------------------------------x");

            Bukkit.shutdown();
        }

        CachedRowSet set = query("SELECT name FROM dustservers.servers WHERE address = ? AND port = ?;", address, port);

        try {
            if (set.next()) {
                String serverName = set.getString("name");
                Core.getInstance().setServerName(serverName);

                return;
            }

            CUtils.inform("Core", "x---------------------------------------------------x");
            CUtils.inform("Core", "This server is not registered in the Oldust database.");
            CUtils.inform("Core", "x---------------------------------------------------x");

            Bukkit.shutdown();
        } catch (SQLException e) {
            CUtils.inform("Core", "x-------------------------------------------------------------x");
            CUtils.inform("Core", "Could not obtain server's public IP address. Validation failed.");
            CUtils.inform("Core", "x-------------------------------------------------------------x");

            Bukkit.shutdown();
        }

    }

    private String getPublicIp(String url) {
        if (fallback) {
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
             BufferedReader in = new BufferedReader(reader)) {

            return in.readLine();
        } catch (IOException e) {
            fallback = true;

            e.printStackTrace();

            return getPublicIp("https://ipv4.icanhazip.com/");
        }

    }

}
