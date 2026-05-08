package carely.repository;

import carely.config.DatabaseConfig;
import carely.error.RepositoryException;
import carely.model.User;
import carely.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

public class UserRepository {
    public User create(User user) {
        String sql = """
                INSERT INTO users (full_name, email, password_hash, role, is_active)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            statement.setBoolean(5, user.isActive());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return findById(generatedKeys.getLong(1))
                            .orElseThrow(() -> new RepositoryException("Created user could not be loaded."));
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to create user.", exception);
        }

        throw new RepositoryException("Failed to create user: no id was generated.");
    }

    public Optional<User> findById(Long id) {
        String sql = """
                SELECT id, full_name, email, password_hash, role, is_active, created_at, updated_at
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to load user.", exception);
        }

        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT id, full_name, email, password_hash, role, is_active, created_at, updated_at
                FROM users
                WHERE LOWER(email) = LOWER(?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to load user by email.", exception);
        }

        return Optional.empty();
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to check user email.", exception);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(UserRole.valueOf(resultSet.getString("role")));
        user.setActive(resultSet.getBoolean("is_active"));
        user.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        user.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return user;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
