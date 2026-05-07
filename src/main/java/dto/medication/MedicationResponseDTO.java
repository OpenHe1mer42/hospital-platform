package dto.medication;

import java.time.Instant;

public class MedicationResponseDTO {
    private String name;
    private Instant createdAt;
    private Instant updatedAt;

    public MedicationResponseDTO(String name, Instant createdAt, Instant updatedAt) {
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
