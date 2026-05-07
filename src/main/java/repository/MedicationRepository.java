package repository;

import error.RepositoryException;
import mapper.MedicationMapper;
import model.Medication;

import javax.xml.transform.Result;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MedicationRepository implements IRepository<Medication> {
    private final MedicationMapper mapper = new MedicationMapper();

    @Override
    public Medication create(Medication obj) {
        String sql = "INSERT INTO medication (name) VALUES (?)";

        try (
                PreparedStatement pstm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        )
        {

            String name = obj.getName();

            pstm.setString(1, name);

            pstm.executeUpdate();

            ResultSet rs = pstm.getGeneratedKeys();

            if(rs.next()) {
                int id = rs.getInt(1);
                return this.getById(id);
            }

            throw new RepositoryException("Failed to create diagnosis", null);
        } catch (SQLException e) {
            throw new RepositoryException("DB error while creating diagnosis", e);
        }
    }

    @Override
    public Medication update(Medication obj) {
        String sql = "UPDATE medication SET name = ? WHERE id = ?";
        try (
                PreparedStatement pstm = connection.prepareStatement(sql);
        )
        {
            String name = obj.getName();
            int id = obj.getId();

            pstm.setString(1, name);
            pstm.setInt(2, id);

            int affectedRows = pstm.executeUpdate();

            if(affectedRows > 0) {
                return this.getById(id);
            }

            throw new RepositoryException("No medication found to update", null);
        } catch (SQLException e) {
            throw new RepositoryException("DB error while updating medication", e);
        }
    }

    @Override
    public Medication getById(int id) {
        String sql = "SELECT * FROM medication WHERE id = ?";

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

            throw new RepositoryException(String.format("Medication with id = %d does not exist!", id), null);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch medication", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM medication WHERE id = ?";

        try (
                PreparedStatement pstm = connection.prepareStatement(sql);
        )
        {
            pstm.setInt(1, id);

            int affectedRows = pstm.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RepositoryException("DB error while deleting medication", e);
        }
    }
}
