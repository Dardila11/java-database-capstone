package com.project.back_end.patient;

import java.util.Optional;

public interface PatientLookup {
    Optional<Patient> findByEmail(String email);
}
