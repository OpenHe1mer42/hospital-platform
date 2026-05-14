package carely.model.dto.diagnosis;

import java.time.LocalDateTime;

public class DiagnosisResponseDTO {
    private Integer id;
    private Long patientId;
    private Long doctorId;
    private String prescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DiagnosisResponseDTO(Integer id, Long patientId, Long doctorId, String prescription,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.prescription = prescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getPrescription() {
        return prescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
