package com.project.back_end.services;

import com.project.back_end.exceptions.InvalidCredentialsException;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    ///  Validate token
    ///  If not valid, throws exception InvalidTokenException("Invalid token")
    public void validateToken(String token, String role) {
        boolean isValid = tokenService.validateToken(token, role);
        if(!isValid) throw new InvalidTokenException("Invalid token");
    }

    ///  Validate if patient exists
    public boolean validatePatient(String email, String phone) {
        return patientRepository.findByEmailOrPhone(email, phone) == null;
    }

    /// Validates patient credentials
    public String validatePatientLogin(String email, String password) {
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null || !patient.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return tokenService.generateToken(patient.getEmail());
    }

    /// Validates admin credentials
    public String validateAdminLogin(String username, String password){
        Admin admin = adminRepository.findByUsername(username);
        if(admin == null || !admin.getPassword().equals(password)){
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return tokenService.generateToken(admin.getUsername());
    }

    /// Validates doctor credentials
    public String validateDoctorLogin(String email, String password){
        Doctor doctor = doctorRepository.findByEmail(email);
        if(doctor == null || !doctor.getPassword().equals(password)){
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return tokenService.generateToken(doctor.getEmail());
    }
}
