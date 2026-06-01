package com.project.back_end.doctor.internal;

import com.project.back_end.shared.RoleValidator;
import org.springframework.stereotype.Component;

@Component
public class DoctorRoleValidator implements RoleValidator {

    private final DoctorRepository doctorRepository;

    public DoctorRoleValidator(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public String role() {
        return "doctor";
    }

    @Override
    public boolean isValidSubject(String email) {
        return doctorRepository.findByEmail(email) != null;
    }
}
