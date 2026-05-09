package carely.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String CONFIG_RESOURCE = "/database.properties";
    private static final String DOTENV_FILE = ".env";
    private static final Properties DOTENV = loadDotenv();
    private static final Properties PROPERTIES = loadProperties();
    private static HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new HikariDataSource(createPoolConfig());
        }
        return dataSource;
    }

    public static synchronized void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    public static String getUrl() {
        return getSetting("DB_URL", "db.url");
    }

    public static String getUsername() {
        return getSetting("DB_USERNAME", "db.username");
    }

    public static String getPassword() {
        String value = getOptionalSetting("DB_PASSWORD", "db.password");
        return value == null ? "" : value;
    }

    public static boolean shouldRunMigrations() {
        String value = getOptionalSetting("CARELY_RUN_MIGRATIONS", "db.runMigrations");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private static HikariConfig createPoolConfig() {
        HikariConfig config = new HikariConfig();
        config.setPoolName(getOptionalSetting("DB_POOL_NAME", "db.pool.name", "CarelyPool"));
        config.setJdbcUrl(getUrl());
        config.setUsername(getUsername());
        config.setPassword(getPassword());
        config.setMaximumPoolSize(getIntSetting("DB_POOL_MAX_SIZE", "db.pool.maximumPoolSize", 10));
        config.setMinimumIdle(getIntSetting("DB_POOL_MIN_IDLE", "db.pool.minimumIdle", 2));
        config.setConnectionTimeout(getLongSetting("DB_POOL_CONNECTION_TIMEOUT_MS", "db.pool.connectionTimeoutMs", 30000));
        config.setIdleTimeout(getLongSetting("DB_POOL_IDLE_TIMEOUT_MS", "db.pool.idleTimeoutMs", 600000));
        config.setMaxLifetime(getLongSetting("DB_POOL_MAX_LIFETIME_MS", "db.pool.maxLifetimeMs", 1800000));
        return config;
    }

    private static String getSetting(String environmentKey, String propertyKey) {
        String value = getOptionalSetting(environmentKey, propertyKey);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing database setting: " + environmentKey + " or " + propertyKey);
        }
        return value;
    }

    private static String getOptionalSetting(String environmentKey, String propertyKey) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null) {
            return environmentValue;
        }
        String dotenvValue = DOTENV.getProperty(environmentKey);
        if (dotenvValue != null) {
            return dotenvValue;
        }
        return PROPERTIES.getProperty(propertyKey);
    }

    private static String getOptionalSetting(String environmentKey, String propertyKey, String defaultValue) {
        String value = getOptionalSetting(environmentKey, propertyKey);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static int getIntSetting(String environmentKey, String propertyKey, int defaultValue) {
        String value = getOptionalSetting(environmentKey, propertyKey);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid integer database setting: " + environmentKey + " or " + propertyKey, exception);
        }
    }

    private static long getLongSetting(String environmentKey, String propertyKey, long defaultValue) {
        String value = getOptionalSetting(environmentKey, propertyKey);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid long database setting: " + environmentKey + " or " + propertyKey, exception);
        }
    }

    private static Properties loadDotenv() {
        Properties properties = new Properties();
        Path dotenvPath = Path.of(DOTENV_FILE);
        if (!Files.isRegularFile(dotenvPath)) {
            return properties;
        }

        try {
            for (String line : Files.readAllLines(dotenvPath)) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                int separatorIndex = trimmedLine.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = trimmedLine.substring(0, separatorIndex).trim();
                String value = trimmedLine.substring(separatorIndex + 1).trim();
                properties.setProperty(key, unquote(value));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read .env database configuration.", exception);
        }

        return properties;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseConfig.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read database configuration.", exception);
        }
        return properties;
    }
}
