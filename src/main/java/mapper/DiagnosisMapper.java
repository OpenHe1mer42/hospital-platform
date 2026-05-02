package mapper;

import dto.IRequestDTO;
import dto.diagnosis.DiagnosisRequestDTO;
import model.Diagnosis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class DiagnosisMapper {


    public Diagnosis fromResultSet(ResultSet rs) {
        try {

            int id = rs.getInt("id");
            int patientId = rs.getInt("patient_id");
            int doctorId = rs.getInt("doctor_id");
            String prescription = rs.getString("prescription");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Instant updatedAt = rs.getTimestamp("updated_at").toInstant();

            return new Diagnosis(id, patientId, doctorId, prescription, updatedAt, createdAt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Diagnosis fromDto(DiagnosisRequestDTO requestDTO) {
        int patientId = requestDTO.getPatientId();
        int doctorId = requestDTO.getDoctorId();
        String prescription = requestDTO.getPrescription();

        return new Diagnosis(null, patientId, doctorId, prescription, null, null);
    }



}
