package dto.diagnosis;

import java.time.Instant;

public class DiagnosisResponseDTO {
    private Integer patientId;
    private Integer doctorId;
    private String prescription;
    private Instant createdAt;
    private Instant updatedAt;

    public DiagnosisResponseDTO(Integer patientId, Integer doctorId, String prescription, Instant createdAt, Instant updatedAt) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.prescription = prescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
