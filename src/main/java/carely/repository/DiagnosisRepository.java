package carely.repository;

import carely.error.RepositoryException;
import carely.model.Diagnosis;
import carely.service.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class DiagnosisRepository implements IRepository<Diagnosis> {
    @Override
    public Diagnosis create(Diagnosis diagnosis) {
        String sql = """
                INSERT INTO diagnosis (patient_id, doctor_id, prescription)
                VALUES (?, ?, ?)
                """;

        try {
            Connection connection = getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, diagnosis.getPatientId());
                statement.setLong(2, diagnosis.getDoctorId());
                statement.setString(3, diagnosis.getPrescription());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return getById(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to create diagnosis.", exception);
        }

        throw new RepositoryException("Failed to create diagnosis: no id was generated.");
    }

    @Override
    public Diagnosis update(Diagnosis diagnosis) {
        String sql = """
                UPDATE diagnosis
                SET patient_id = ?, doctor_id = ?, prescription = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try {
            Connection connection = getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, diagnosis.getPatientId());
                statement.setLong(2, diagnosis.getDoctorId());
                statement.setString(3, diagnosis.getPrescription());
                statement.setInt(4, diagnosis.getId());

                if (statement.executeUpdate() == 0) {
                    throw new RepositoryException("Diagnosis not found.");
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to update diagnosis.", exception);
        }

        return getById(diagnosis.getId());
    }

    @Override
    public Diagnosis getById(int id) {
        String sql = """
                SELECT id, patient_id, doctor_id, prescription, created_at, updated_at
                FROM diagnosis
                WHERE id = ?
                """;

        try {
            Connection connection = getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapRow(resultSet);
                    }
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to load diagnosis.", exception);
        }

        return null;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM diagnosis WHERE id = ?";

        try {
            Connection connection = getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Failed to delete diagnosis.", exception);
        }
    }

    private Connection getConnection() {
        Connection connection = DatabaseService.getConnection();
        if (connection == null) {
            throw new RepositoryException("Database connection is not configured or could not be opened.");
        }
        return connection;
    }

    private Diagnosis mapRow(ResultSet resultSet) throws SQLException {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(resultSet.getInt("id"));
        diagnosis.setPatientId(resultSet.getLong("patient_id"));
        diagnosis.setDoctorId(resultSet.getLong("doctor_id"));
        diagnosis.setPrescription(resultSet.getString("prescription"));
        diagnosis.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        diagnosis.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return diagnosis;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
