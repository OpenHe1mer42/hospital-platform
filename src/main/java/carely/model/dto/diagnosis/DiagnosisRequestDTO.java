package carely.model.dto.diagnosis;

public class DiagnosisRequestDTO {
    private Long patientId;
    private Long doctorId;
    private String prescription;

    public DiagnosisRequestDTO() {
    }

    public DiagnosisRequestDTO(Long patientId, Long doctorId, String prescription) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.prescription = prescription;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
}
