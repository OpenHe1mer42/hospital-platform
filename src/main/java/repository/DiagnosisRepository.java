package repository;

import dto.diagnosis.DiagnosisRequestDTO;
import error.RepositoryException;
import mapper.DiagnosisMapper;
import model.Diagnosis;
import service.DatabaseService;

import java.sql.*;


public class DiagnosisRepository implements IRepository<Diagnosis> {
    // create, update, getById, delete
    private static final DiagnosisMapper mapper = new DiagnosisMapper();

    public Diagnosis create(Diagnosis obj) throws RepositoryException {
        String sql = "INSERT INTO diagnosis (patient_id, doctor_id, prescription) VALUES (?, ?, ?)";

        try (PreparedStatement pstm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);)
        {
            int patientId = obj.getPatientId();
            int doctorId = obj.getDoctorId();
            String prescription = obj.getPrescription();

            pstm.setInt(1, patientId);
            pstm.setInt(2, doctorId);
            pstm.setString(3, prescription);

            pstm.executeUpdate();

            ResultSet rs = pstm.getGeneratedKeys();

            if(rs.next()) {
                int id = rs.getInt(1);
                return this.getById(id);
            }

            throw new RepositoryException("Failed to retrieve created diagnosis", null);
        } catch(SQLException e) {
            throw new RepositoryException("DB error while creating diagnosis", e);
        }
    }

    public Diagnosis getById(int id) throws RepositoryException {
        String sql = "SELECT * FROM diagnosis WHERE id = ?";

        try (
            PreparedStatement pstm = connection.prepareStatement(sql);
        )
        {
            pstm.setInt(1, id);

            try (ResultSet rs = pstm.executeQuery();) {
                if(rs.next()) {
                    return mapper.fromResultSet(rs);
                }
            }

            throw new RepositoryException(String.format("Diagnosis with id = %d does not exist!", id), null);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch diagnosis", e);
        }
    }

    public Diagnosis update(Diagnosis obj) throws RepositoryException {
        String sql = "UPDATE diagnosis SET prescription = ? WHERE id = ?";
        try (
           PreparedStatement pstm = connection.prepareStatement(sql);
        )
        {
            String prescription = obj.getPrescription();
            int id = obj.getId();

            pstm.setString(1, prescription);
            pstm.setInt(2, id);

            int affectedRows = pstm.executeUpdate();

            if(affectedRows > 0) {
                return this.getById(id);
            }

            throw new RepositoryException("No diagnosis found to update", null);
        } catch (SQLException e) {
            throw new RepositoryException("DB error while updating diagnosis", e);
        }
    }

    public boolean delete(int id) throws RepositoryException {
        String sql = "DELETE FROM diagnosis WHERE id = ?";

        try (
                PreparedStatement pstm = connection.prepareStatement(sql);
        )
        {
            pstm.setInt(1, id);

            int affectedRows = pstm.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RepositoryException("DB error while deleting diagnosis", e);
        }
    }
}
