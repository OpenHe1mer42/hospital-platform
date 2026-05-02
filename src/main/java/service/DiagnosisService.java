package service;

import dto.diagnosis.DiagnosisRequestDTO;
import error.RepositoryException;
import mapper.DiagnosisMapper;
import model.Diagnosis;
import repository.DiagnosisRepository;

import java.sql.SQLException;

public class DiagnosisService {

    DiagnosisMapper mapper;
    DiagnosisRepository repository;

    public DiagnosisService() {
        this.mapper = new DiagnosisMapper();
        this.repository = new DiagnosisRepository();
    }

    public Diagnosis create(DiagnosisRequestDTO requestDTO) throws RuntimeException {
        this.validate(requestDTO);

        Diagnosis diagnosis = this.mapper.fromDto(requestDTO);
        return this.repository.create(diagnosis);
    }

    public Diagnosis update(Integer id, DiagnosisRequestDTO requestDTO) throws RuntimeException {
        this.validate(id, requestDTO);

        Diagnosis diagnosis = this.mapper.fromDto(requestDTO);
        diagnosis.setId(id);

        return this.repository.update(diagnosis);
    }

    public boolean delete(Integer id) throws RuntimeException {
        this.validate(id);
        boolean deleted = this.repository.delete(id);

        if(!deleted) {
            throw new RuntimeException("Diagnosis not found!");
        }

        return deleted;
}

    public Diagnosis getById(Integer id) throws RuntimeException {
        this.validate(id);

        return this.repository.getById(id);
    }

    private void validate(DiagnosisRequestDTO requestDTO) throws IllegalArgumentException {
        if(requestDTO.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor ID must not be null!");
        } else if(requestDTO.getPatientId() == null) {
            throw new IllegalArgumentException("Patient ID must not be null!");
        } else if(requestDTO.getPrescription() == null || requestDTO.getPrescription().isEmpty()) {
            throw new IllegalArgumentException("Prescription must not be null or empty!");
        }
    }

    private void validate(Integer id) throws IllegalArgumentException {
        if(id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
    }

    private void validate(Integer id, DiagnosisRequestDTO requestDTO) throws IllegalArgumentException {
        this.validate(requestDTO);
        this.validate(id);
    }
}
