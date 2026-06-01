package com.project.back_end.patient.internal;

import java.util.Optional;

import com.project.back_end.enums.ServiceResult;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.exceptions.NotFoundException;
import com.project.back_end.patient.Patient;
import com.project.back_end.patient.PatientLookup;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.back_end.shared.TokenService;

@org.springframework.stereotype.Service
public class PatientService implements PatientLookup {

    private final PatientRepository patientRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PatientService(PatientRepository patientRepository,
                          TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<Patient> findByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmailOrPhone(String email, String phone) {
        return patientRepository.findByEmailOrPhone(email, phone) != null;
    }

    public ServiceResult createPatient(Patient patient) {
        try {
            patient.setPassword(passwordEncoder.encode(patient.getPassword()));
            patientRepository.save(patient);
            return ServiceResult.SUCCESS;
        } catch (Exception e) {
            return ServiceResult.CONFLICT;
        }
    }

    public Patient getPatientDetails(String token) {
        String email = tokenService.extractEmail(token);
        if (email == null) throw new InvalidTokenException("Invalid token");
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
    }
}