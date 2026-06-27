package dev.oum.oumlib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.scheduler.Promise;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class Database {

    private final HikariDataSource dataSource;
    private final Map<Class<?>, RowMapper<?>> mapperCache = new ConcurrentHashMap<>();
    private long slowQueryThresholdMs = 50;

    private Database(@NonNull HikariConfig config) {
        this.dataSource = new HikariDataSource(config);
    }

    @CheckReturnValue
    public static @NonNull Database sqlite(@NonNull File file) {
        return sqlite(file, cfg -> {
        });
    }

    @CheckReturnValue
    public static @NonNull Database sqlite(@NonNull File file, @NonNull Consumer<HikariConfig> configCustomizer) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");

        configCustomizer.accept(config);
        return new Database(config);
    }

    @CheckReturnValue
    public static @NonNull Database mysql(
            @NonNull String host,
            int port,
            @NonNull String database,
            @NonNull String username,
            @NonNull String password
    ) {
        return mysql(host, port, database, username, password, cfg -> {
        });
    }

    @CheckReturnValue
    public static @NonNull Database mysql(
            @NonNull String host,
            int port,
            @NonNull String database,
            @NonNull String username,
            @NonNull String password,
            @NonNull Consumer<HikariConfig> configCustomizer
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);

        configCustomizer.accept(config);
        return new Database(config);
    }

    @CheckReturnValue
    public static @NonNull Database custom(@NonNull HikariConfig config) {
        return new Database(config);
    }

    private static @Nullable Object getValueFromResultSet(@NonNull ResultSet rs, @NonNull String label,
                                                          @NonNull Class<?> type) throws SQLException {
        Object val = rs.getObject(label);
        if (val == null) return null;
        if (type == int.class || type == Integer.class) {
            return rs.getInt(label);
        } else if (type == double.class || type == Double.class) {
            return rs.getDouble(label);
        } else if (type == float.class || type == Float.class) {
            return rs.getFloat(label);
        } else if (type == long.class || type == Long.class) {
            return rs.getLong(label);
        } else if (type == boolean.class || type == Boolean.class) {
            return rs.getBoolean(label);
        } else if (type == String.class) {
            return rs.getString(label);
        }
        return val;
    }

    private static @NonNull String toCamelCase(@NonNull String snake) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < snake.length(); i++) {
            char c = snake.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    private static @NonNull String toSnakeCase(@NonNull String camel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static @NonNull Set<String> columnLabels(@NonNull ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        Set<String> labels = new HashSet<>(columns * 2);
        for (int i = 1; i <= columns; i++) {
            labels.add(md.getColumnLabel(i));
        }
        return labels;
    }

    private static int parseVersion(@NonNull String filename) {
        try {
            int underscoreIdx = filename.indexOf("__");
            if (underscoreIdx != -1) {
                return Integer.parseInt(filename.substring(1, underscoreIdx));
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }

    private static @NonNull String parseDescription(@NonNull String filename) {
        int underscoreIdx = filename.indexOf("__");
        if (underscoreIdx != -1) {
            String desc = filename.substring(underscoreIdx + 2);
            if (desc.endsWith(".sql")) {
                desc = desc.substring(0, desc.length() - 4);
            }
            return desc.replace('_', ' ');
        }
        return filename;
    }

    public @NonNull Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public @NonNull HikariDataSource dataSource() {
        return dataSource;
    }

    public long getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }

    public void setSlowQueryThresholdMs(long ms) {
        this.slowQueryThresholdMs = ms;
    }

    @CheckReturnValue
    public @NonNull Promise<Integer> executeUpdate(@NonNull String sql, Object... params) {
        return Promise.supplyVirtual(() -> {
            long start = System.currentTimeMillis();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                int affected = stmt.executeUpdate();
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > slowQueryThresholdMs) {
                    OumLib.logWarning("SLOW UPDATE QUERY (" + elapsed + "ms): " + sql);
                }
                return affected;
            } catch (SQLException e) {
                throw new RuntimeException("SQL update execution failed: " + sql, e);
            }
        });
    }

    /**
     * Executes a SELECT query asynchronously using virtual threads and returns mapped results.
     */
    @CheckReturnValue
    public @NonNull Promise<List<Map<String, Object>>> executeQuery(@NonNull String sql, Object... params) {
        return Promise.supplyVirtual(() -> {
            long start = System.currentTimeMillis();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();
                    List<Map<String, Object>> list = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>(columns);
                        for (int i = 1; i <= columns; i++) {
                            row.put(md.getColumnLabel(i), rs.getObject(i));
                        }
                        list.add(row);
                    }
                    long elapsed = System.currentTimeMillis() - start;
                    if (elapsed > slowQueryThresholdMs) {
                        OumLib.logWarning("SLOW SELECT QUERY (" + elapsed + "ms): " + sql);
                    }
                    return list;
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL query execution failed: " + sql, e);
            }
        });
    }

    @CheckReturnValue
    public <T> @NonNull Promise<List<T>> executeQuery(@NonNull String sql, @NonNull RowMapper<T> mapper, Object... params) {
        return Promise.supplyVirtual(() -> {
            long start = System.currentTimeMillis();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    List<T> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.map(rs));
                    }
                    long elapsed = System.currentTimeMillis() - start;
                    if (elapsed > slowQueryThresholdMs) {
                        OumLib.logWarning("SLOW SELECT QUERY (" + elapsed + "ms): " + sql);
                    }
                    return list;
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL query execution failed: " + sql, e);
            }
        });
    }

    @CheckReturnValue
    public <T> @NonNull Promise<List<T>> executeQuery(@NonNull String sql, @NonNull Class<T> type, Object... params) {
        return executeQuery(sql, reflectiveMapper(type), params);
    }

    @SuppressWarnings("unchecked")
    private <T> @NonNull RowMapper<T> reflectiveMapper(@NonNull Class<T> type) {
        return (RowMapper<T>) mapperCache.computeIfAbsent(type, Database::buildMapper);
    }

    private static @NonNull RowMapper<?> buildMapper(@NonNull Class<?> type) {
        if (type.isRecord()) {
            RecordComponent[] components = type.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                paramTypes[i] = components[i].getType();
            }
            Constructor<?> ctor;
            try {
                ctor = type.getDeclaredConstructor(paramTypes);
                ctor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("No canonical constructor for record " + type.getName(), e);
            }
            return rs -> {
                try {
                    Set<String> available = columnLabels(rs);
                    Object[] args = new Object[components.length];
                    for (int i = 0; i < components.length; i++) {
                        String name = components[i].getName();
                        String label = available.contains(name) ? name
                                : (available.contains(toSnakeCase(name)) ? toSnakeCase(name) : null);
                        if (label == null) {
                            OumLib.logDebug("Unmapped record component '" + name + "' for " + type.getName());
                            args[i] = null;
                        } else {
                            args[i] = getValueFromResultSet(rs, label, components[i].getType());
                        }
                    }
                    return ctor.newInstance(args);
                } catch (ReflectiveOperationException e) {
                    throw new SQLException("Failed to map row to record " + type.getName(), e);
                }
            };
        }

        Map<String, Field> fields = new HashMap<>();
        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            fields.put(field.getName(), field);
        }
        return rs -> {
            try {
                Object instance = type.getDeclaredConstructor().newInstance();
                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();
                for (int i = 1; i <= columns; i++) {
                    String label = md.getColumnLabel(i);
                    Field field = fields.get(label);
                    if (field == null) {
                        field = fields.get(toCamelCase(label));
                    }
                    if (field == null) {
                        OumLib.logDebug("Unmapped column '" + label + "' for " + type.getName());
                        continue;
                    }
                    field.set(instance, getValueFromResultSet(rs, label, field.getType()));
                }
                return instance;
            } catch (ReflectiveOperationException e) {
                throw new SQLException("Failed to map row to class " + type.getName(), e);
            }
        };
    }

    @CheckReturnValue
    public @NonNull Promise<int[]> executeBatch(@NonNull String sql, @NonNull List<Object[]> parameterBatch) {
        return Promise.supplyVirtual(() -> {
            long start = System.currentTimeMillis();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Object[] params : parameterBatch) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                    stmt.addBatch();
                }
                int[] results = stmt.executeBatch();
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > slowQueryThresholdMs) {
                    OumLib.logWarning("SLOW BATCH QUERY (" + elapsed + "ms): " + sql);
                }
                return results;
            } catch (SQLException e) {
                throw new RuntimeException("SQL batch execution failed: " + sql, e);
            }
        });
    }

    @CheckReturnValue
    public <R> @NonNull Promise<R> transaction(@NonNull TransactionContextCallback<R> callback) {
        return Promise.supplyVirtual(() -> {
            long start = System.currentTimeMillis();
            try (Connection conn = getConnection()) {
                boolean wasAutoCommit = conn.getAutoCommit();
                try {
                    conn.setAutoCommit(false);
                    TransactionContext ctx = new TransactionContext(conn, slowQueryThresholdMs);
                    R result = callback.execute(ctx);
                    conn.commit();
                    long elapsed = System.currentTimeMillis() - start;
                    if (elapsed > slowQueryThresholdMs) {
                        OumLib.logWarning("SLOW TRANSACTION (" + elapsed + "ms)");
                    }
                    return result;
                } catch (Throwable t) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        t.addSuppressed(ex);
                    }
                    throw t;
                } finally {
                    try {
                        conn.setAutoCommit(wasAutoCommit);
                    } catch (SQLException ignored) {
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL transaction execution failed", e);
            }
        });
    }

    @CheckReturnValue
    public @NonNull Promise<Void> executeScript(@NonNull String sqlScript) {
        return Promise.runVirtual(() -> {
            try (Connection conn = getConnection()) {
                String[] statements = sqlScript.split(";");
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : statements) {
                        String trimmed = sql.replaceAll("(?m)^\\s*--.*$", "")
                                .replaceAll("(?s)/\\*.*?\\*/", "")
                                .trim();
                        if (!trimmed.isEmpty()) {
                            stmt.execute(trimmed);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL script execution failed", e);
            }
        });
    }

    @CheckReturnValue
    public @NonNull Promise<Void> executeScript(@NonNull InputStream stream) {
        return Promise.runVirtual(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                executeScript(sb.toString()).toCompletableFuture().join();
            } catch (Exception e) {
                throw new RuntimeException("SQL script loading failed", e);
            }
        });
    }

    public void runMigrations(@NonNull File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.startsWith("V") && name.endsWith(".sql"));
        if (files == null) return;
        Arrays.sort(files, Comparator.comparing(File::getName));

        executeUpdate("CREATE TABLE IF NOT EXISTS oumlib_schema_history (" +
                "version INTEGER PRIMARY KEY, " +
                "description VARCHAR(200) NOT NULL, " +
                "script VARCHAR(200) NOT NULL, " +
                "installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")").toCompletableFuture().join();

        for (File file : files) {
            String name = file.getName();
            int version = parseVersion(name);
            if (version == -1) continue;

            List<Map<String, Object>> rows = executeQuery("SELECT version FROM oumlib_schema_history WHERE version = ?", version)
                    .toCompletableFuture().join();
            if (rows.isEmpty()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    executeScript(fis).toCompletableFuture().join();
                    executeUpdate("INSERT INTO oumlib_schema_history (version, description, script) VALUES (?, ?, ?)",
                            version, parseDescription(name), name).toCompletableFuture().join();
                } catch (Exception e) {
                    throw new RuntimeException("Migration " + name + " failed!", e);
                }
            }
        }
    }

    public void runMigrations(@NonNull Class<?> resourceLoaderClass, String... resourcePaths) {
        executeUpdate("CREATE TABLE IF NOT EXISTS oumlib_schema_history (" +
                "version INTEGER PRIMARY KEY, " +
                "description VARCHAR(200) NOT NULL, " +
                "script VARCHAR(200) NOT NULL, " +
                "installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")").toCompletableFuture().join();

        Arrays.sort(resourcePaths, Comparator.comparing(path -> {
            String filename = path.substring(path.lastIndexOf('/') + 1);
            return parseVersion(filename);
        }));

        for (String path : resourcePaths) {
            String filename = path.substring(path.lastIndexOf('/') + 1);
            int version = parseVersion(filename);
            if (version == -1) continue;

            List<Map<String, Object>> rows = executeQuery("SELECT version FROM oumlib_schema_history WHERE version = ?", version)
                    .toCompletableFuture().join();
            if (rows.isEmpty()) {
                try (InputStream is = resourceLoaderClass.getClassLoader().getResourceAsStream(path)) {
                    if (is == null) {
                        throw new IllegalArgumentException("Resource not found: " + path);
                    }
                    executeScript(is).toCompletableFuture().join();
                    executeUpdate("INSERT INTO oumlib_schema_history (version, description, script) VALUES (?, ?, ?)",
                            version, parseDescription(filename), filename).toCompletableFuture().join();
                } catch (Exception e) {
                    throw new RuntimeException("Migration " + filename + " failed!", e);
                }
            }
        }
    }
}
