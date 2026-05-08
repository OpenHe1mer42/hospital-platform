ALTER TABLE users
    ADD CONSTRAINT fk_users_address FOREIGN KEY (address_id) REFERENCES address (id),
    ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE allergy_user
    DROP FOREIGN KEY allergy_user_ibfk_1;
ALTER TABLE allergy_user
    MODIFY patient_id BIGINT NOT NULL;
ALTER TABLE allergy_user
    ADD CONSTRAINT allergy_user_ibfk_1 FOREIGN KEY (patient_id) REFERENCES users (id);

ALTER TABLE appointment
    DROP FOREIGN KEY appointment_ibfk_1,
    DROP FOREIGN KEY appointment_ibfk_2,
    DROP FOREIGN KEY appointment_ibfk_3;
ALTER TABLE appointment
    MODIFY patient_id BIGINT NOT NULL,
    MODIFY doctor_id BIGINT NOT NULL,
    MODIFY updated_by BIGINT NULL;
ALTER TABLE appointment
    ADD CONSTRAINT appointment_ibfk_1 FOREIGN KEY (patient_id) REFERENCES users (id),
    ADD CONSTRAINT appointment_ibfk_2 FOREIGN KEY (doctor_id) REFERENCES users (id),
    ADD CONSTRAINT appointment_ibfk_3 FOREIGN KEY (updated_by) REFERENCES users (id);

ALTER TABLE diagnosis
    DROP FOREIGN KEY diagnosis_ibfk_1,
    DROP FOREIGN KEY diagnosis_ibfk_2;
ALTER TABLE diagnosis
    MODIFY patient_id BIGINT NOT NULL,
    MODIFY doctor_id BIGINT NOT NULL;
ALTER TABLE diagnosis
    ADD CONSTRAINT diagnosis_ibfk_1 FOREIGN KEY (patient_id) REFERENCES users (id),
    ADD CONSTRAINT diagnosis_ibfk_2 FOREIGN KEY (doctor_id) REFERENCES users (id);

ALTER TABLE lab_test
    DROP FOREIGN KEY lab_test_ibfk_1;
ALTER TABLE lab_test
    MODIFY patient_id BIGINT NOT NULL;
ALTER TABLE lab_test
    ADD CONSTRAINT lab_test_ibfk_1 FOREIGN KEY (patient_id) REFERENCES users (id);

ALTER TABLE medical_referral
    DROP FOREIGN KEY medical_referral_ibfk_1,
    DROP FOREIGN KEY medical_referral_ibfk_2;
ALTER TABLE medical_referral
    MODIFY patient_id BIGINT NOT NULL,
    MODIFY doctor_id BIGINT NOT NULL;
ALTER TABLE medical_referral
    ADD CONSTRAINT medical_referral_ibfk_1 FOREIGN KEY (patient_id) REFERENCES users (id),
    ADD CONSTRAINT medical_referral_ibfk_2 FOREIGN KEY (doctor_id) REFERENCES users (id);

ALTER TABLE sick_note
    DROP FOREIGN KEY sick_note_ibfk_1,
    DROP FOREIGN KEY sick_note_ibfk_2;
ALTER TABLE sick_note
    MODIFY patient_id BIGINT NOT NULL,
    MODIFY approved_by BIGINT NOT NULL;
ALTER TABLE sick_note
    ADD CONSTRAINT sick_note_ibfk_1 FOREIGN KEY (patient_id) REFERENCES users (id),
    ADD CONSTRAINT sick_note_ibfk_2 FOREIGN KEY (approved_by) REFERENCES users (id);

ALTER TABLE user_disease
    DROP FOREIGN KEY user_disease_ibfk_1;
ALTER TABLE user_disease
    MODIFY patient_id BIGINT NOT NULL;
ALTER TABLE user_disease
    ADD CONSTRAINT user_disease_ibfk_1 FOREIGN KEY (patient_id) REFERENCES users (id);

ALTER TABLE user_medication
    DROP FOREIGN KEY user_medication_ibfk_1;
ALTER TABLE user_medication
    MODIFY patient_id BIGINT NOT NULL;
ALTER TABLE user_medication
    ADD CONSTRAINT user_medication_ibfk_1 FOREIGN KEY (patient_id) REFERENCES users (id);

DROP TABLE user;
