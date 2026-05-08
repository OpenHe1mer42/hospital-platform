package carely.config;

import carely.error.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MigrationRunner {
    private static final List<String> MIGRATIONS = List.of(
            "V001__create_users.sql",
            "V002__add_profile_fields_to_users.sql",
            "V003__point_foreign_keys_to_users.sql"
    );

    public void runMigrations() {
        try (Connection connection = DatabaseConfig.getConnection()) {
            ensureMigrationTable(connection);
            for (String migration : MIGRATIONS) {
                runMigrationIfNeeded(connection, migration);
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to run database migrations.", exception);
        }
    }

    public static void main(String[] args) {
        new MigrationRunner().runMigrations();
        System.out.println("Carely migrations completed.");
    }

    private void ensureMigrationTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS schema_migrations (
                    version VARCHAR(80) PRIMARY KEY,
                    description VARCHAR(255) NOT NULL,
                    installed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void runMigrationIfNeeded(Connection connection, String migrationFile) throws SQLException {
        String version = migrationFile.substring(0, migrationFile.indexOf("__"));
        if (isApplied(connection, version)) {
            return;
        }

        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            executeMigration(connection, readMigration(migrationFile));
            recordMigration(connection, version, describe(migrationFile));
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private boolean isApplied(Connection connection, String version) throws SQLException {
        String sql = "SELECT 1 FROM schema_migrations WHERE version = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, version);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void executeMigration(Connection connection, String sql) throws SQLException {
        for (String statementSql : sql.split(";")) {
            String trimmedSql = statementSql.trim();
            if (trimmedSql.isEmpty()) {
                continue;
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(trimmedSql);
            }
        }
    }

    private void recordMigration(Connection connection, String version, String description) throws SQLException {
        String sql = "INSERT INTO schema_migrations (version, description) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, version);
            statement.setString(2, description);
            statement.executeUpdate();
        }
    }

    private String readMigration(String migrationFile) {
        String resource = "/migrations/" + migrationFile;
        try (InputStream inputStream = MigrationRunner.class.getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new RepositoryException("Migration file not found: " + migrationFile);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new RepositoryException("Failed to read migration file: " + migrationFile, exception);
        }
    }

    private String describe(String migrationFile) {
        return migrationFile
                .substring(migrationFile.indexOf("__") + 2, migrationFile.lastIndexOf(".sql"))
                .replace('_', ' ');
    }
}
