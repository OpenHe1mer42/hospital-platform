package dto.diagnosis;

import dto.IRequestDTO;

public class DiagnosisRequestDTO implements IRequestDTO {
    private Integer patientId;
    private Integer doctorId;
    private String prescription;

    public DiagnosisRequestDTO(Integer patientId, Integer doctorId, String prescription) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.prescription = prescription;
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
}
