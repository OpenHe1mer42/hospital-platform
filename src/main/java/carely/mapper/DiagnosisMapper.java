package carely.mapper;

import carely.model.Diagnosis;
import carely.model.dto.diagnosis.DiagnosisRequestDTO;
import carely.model.dto.diagnosis.DiagnosisResponseDTO;

public class DiagnosisMapper {
    public Diagnosis fromDto(DiagnosisRequestDTO requestDTO) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setPatientId(requestDTO.getPatientId());
        diagnosis.setDoctorId(requestDTO.getDoctorId());
        diagnosis.setPrescription(requestDTO.getPrescription().trim());
        return diagnosis;
    }

    public DiagnosisResponseDTO toDto(Diagnosis diagnosis) {
        return new DiagnosisResponseDTO(
                diagnosis.getId(),
                diagnosis.getPatientId(),
                diagnosis.getDoctorId(),
                diagnosis.getPrescription(),
                diagnosis.getCreatedAt(),
                diagnosis.getUpdatedAt()
        );
    }
}
