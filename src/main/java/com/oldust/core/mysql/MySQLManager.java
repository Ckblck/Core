package com.oldust.core.mysql;

import com.oldust.core.Core;
import com.oldust.core.utils.CUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MySQLManager {
    private static HikariDataSource pool;
    private final File credentialsFile = new File(Core.getInstance().getDataFolder(), "db_credentials.yml");

    public MySQLManager() {
        if (!credentialsFile.exists())
            createFile();

        try {
            HikariConfig config = new HikariConfig();
            YamlConfiguration yamlConfig = new YamlConfiguration();

            yamlConfig.load(credentialsFile);

            String hostName = yamlConfig.getString("host-name");
            String password = yamlConfig.getString("password");
            String username = yamlConfig.getString("username");
            int port = yamlConfig.getInt("port");

            config.setJdbcUrl("jdbc:mysql://" + hostName + ":" + port + "/?autoReconnect=true&allowMultiQueries=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false");
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setUsername(username);
            config.setPassword(password);
            config.setConnectionTimeout(7000);
            config.addDataSourceProperty("cachePrepStmts", true);

            pool = new HikariDataSource(config);
        } catch (Exception e) {
            CUtils.inform("DB", "No ha sido posible iniciar la conexión a la base de datos.");
            e.printStackTrace();

            Bukkit.shutdown();
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
        Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), () -> future.complete(query(statement, placeholders)));
    }

    public static CachedRowSet query(String statement, Object... placeholders) {
        try (Connection conn = pool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);

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

    public static void updateThrowable(String statement, Object... placeholders) throws SQLException {
        try (Connection conn = pool.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(statement);

            for (int i = 0; i < placeholders.length; i++) {
                ps.setObject(i + 1, placeholders[i]);
            }

            ps.executeUpdate();
        }
    }

    public static void updateAsync(String statement, Object... placeholders) {
        Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), () -> update(statement, placeholders));
    }

    public static void update(String statement, Object... placeholders) {
        try {
            updateThrowable(statement, placeholders);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createFile() {
        File file = new File(credentialsFile.toURI());

        try {
            FileUtils.copyInputStreamToFile(Objects.requireNonNull(Core.getInstance().getResource("db_credentials.yml")), credentialsFile);
            Files.copy(file.toPath(), credentialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            CUtils.inform("DB", "Se ha creado el archivo de credenciales para la base de datos por primera vez. Rellénalo y reinicia el servidor.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.shutdown();
    }

}