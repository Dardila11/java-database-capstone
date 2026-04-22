package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;

    public ValidationService(TokenService tokenService, PatientRepository patientRepository, AdminRepository adminRepository, DoctorRepository doctorRepository) {
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
    }

    public boolean validateToken(String token, String role) {
        return tokenService.validateToken(token, role);
    }

    ///  Validate if patient exists
    public boolean validatePatient(String email, String phone) {
        return patientRepository.findByEmailOrPhone(email, phone) == null;
    }

    /// Validates patient credentials
    public boolean validatePatientLogin(String email, String password) {
        Patient patient = patientRepository.findByEmail(email);
        return patient != null && patient.getPassword().equals(password);
    }

    /// Validates admin credentials
    public boolean validateAdminLogin(String username, String password){
        Admin admin = adminRepository.findByUsername(username);
        return admin != null && admin.getPassword().equals(password);
    }

    /// Validates doctor credentials
    public boolean validateDoctorLogin(String email, String password){
        Doctor doctor = doctorRepository.findByEmail(email);
        return doctor != null && doctor.getPassword().equals(password);
    }

}
