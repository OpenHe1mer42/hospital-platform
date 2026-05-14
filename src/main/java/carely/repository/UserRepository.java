package carely.repository;

import carely.config.DatabaseConfig;
import carely.error.RepositoryException;
import carely.model.User;
import carely.model.UserRole;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class UserRepository {
    private final DataSource dataSource;

    public UserRepository() {
        this(DatabaseConfig.getDataSource());
    }

    public UserRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    public User create(User user) {
        String sql = """
                INSERT INTO users (full_name, email, password_hash, role, is_active)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            statement.setBoolean(5, user.isActive());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return findById(connection, generatedKeys.getLong(1))
                            .orElseThrow(() -> new RepositoryException("Created user could not be loaded."));
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to create user.", exception);
        }

        throw new RepositoryException("Failed to create user: no id was generated.");
    }

    public Optional<User> findById(Long id) {
        try (Connection connection = dataSource.getConnection()) {
            return findById(connection, id);
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to load user.", exception);
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT id, full_name, email, password_hash, phone, gender, date_of_birth, role, is_active, created_at, updated_at
                FROM users
                WHERE LOWER(email) = LOWER(?)
                """;

        try (Connection connection = dataSource.getConnection();
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

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to check user email.", exception);
        }
    }

    public boolean existsByEmailForOtherUser(String email, Long userId) {
        String sql = """
                SELECT 1
                FROM users
                WHERE LOWER(email) = LOWER(?)
                  AND id <> ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to check user email.", exception);
        }
    }

    public User updateProfile(User user) {
        String sql = """
                UPDATE users
                SET full_name = ?,
                    email = ?,
                    phone = ?,
                    gender = ?,
                    date_of_birth = ?
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getGender());
            setNullableDate(statement, 5, user.getDateOfBirth());
            statement.setLong(6, user.getId());

            if (statement.executeUpdate() == 0) {
                throw new RepositoryException("Profile could not be updated because the user was not found.");
            }

            return findById(connection, user.getId())
                    .orElseThrow(() -> new RepositoryException("Updated user could not be loaded."));
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to update profile.", exception);
        }
    }

    public void updatePassword(Long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setLong(2, userId);

            if (statement.executeUpdate() == 0) {
                throw new RepositoryException("Password could not be updated because the user was not found.");
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to update password.", exception);
        }
    }

    public User deactivate(Long userId) {
        String sql = "UPDATE users SET is_active = FALSE WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            if (statement.executeUpdate() == 0) {
                throw new RepositoryException("Account could not be deactivated because the user was not found.");
            }

            return findById(connection, userId)
                    .orElseThrow(() -> new RepositoryException("Deactivated user could not be loaded."));
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to deactivate account.", exception);
        }
    }

    private Optional<User> findById(Connection connection, Long id) throws SQLException {
        String sql = """
                SELECT id, full_name, email, password_hash, phone, gender, date_of_birth, role, is_active, created_at, updated_at
                FROM users
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setPhone(resultSet.getString("phone"));
        user.setGender(resultSet.getString("gender"));
        user.setDateOfBirth(toLocalDate(resultSet.getDate("date_of_birth")));
        user.setRole(UserRole.valueOf(resultSet.getString("role")));
        user.setActive(resultSet.getBoolean("is_active"));
        user.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        user.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return user;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private void setNullableDate(PreparedStatement statement, int parameterIndex, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setDate(parameterIndex, null);
        } else {
            statement.setDate(parameterIndex, Date.valueOf(value));
        }
    }
}
