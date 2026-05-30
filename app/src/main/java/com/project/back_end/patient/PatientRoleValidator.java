package com.project.back_end.patient;

import com.project.back_end.shared.RoleValidator;
import org.springframework.stereotype.Component;

@Component
public class PatientRoleValidator implements RoleValidator {

    private final PatientRepository patientRepository;

    public PatientRoleValidator(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public String role() {
        return "patient";
    }

    @Override
    public boolean isValidSubject(String email) {
        return patientRepository.findByEmail(email).isPresent();
    }
}
