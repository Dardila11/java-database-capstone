package com.project.back_end.services;

import com.project.back_end.exceptions.InvalidCredentialsException;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValidationService {

    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public ValidationService(TokenService tokenService, PatientRepository patientRepository,
                             AdminRepository adminRepository, DoctorRepository doctorRepository,
                             PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    ///  Validate token
    ///  If not valid, throws exception InvalidTokenException("Invalid token")
    public void validateToken(String token, String role) {
        boolean isValid = tokenService.validateToken(token, role);
        if(!isValid) throw new InvalidTokenException("Invalid token");
    }

    public boolean validatePatient(String email, String phone) {
        return patientRepository.findByEmailOrPhone(email, phone) == null;
    }

    public String validatePatientLogin(String email, String password) {
        Optional<Patient> opt = patientRepository.findByEmail(email);
        if (opt.isEmpty()) throw new InvalidCredentialsException("Invalid email or password");

        Patient patient = opt.get();
        String stored = patient.getPassword();

        if (passwordEncoder.matches(password, stored)) {
            // already BCrypt-hashed
        } else if (password.equals(stored)) {
            // plain-text legacy password — migrate to BCrypt on successful login
            patient.setPassword(passwordEncoder.encode(password));
            patientRepository.save(patient);
        } else {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return tokenService.generateToken(patient.getEmail());
    }

    public String validateAdminLogin(String username, String password) {
        Admin admin = adminRepository.findByUsername(username);
        if (admin == null) throw new InvalidCredentialsException("Invalid username or password");

        String stored = admin.getPassword();

        if (passwordEncoder.matches(password, stored)) {
            // already BCrypt-hashed
        } else if (password.equals(stored)) {
            admin.setPassword(passwordEncoder.encode(password));
            adminRepository.save(admin);
        } else {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return tokenService.generateToken(admin.getUsername());
    }

    public String validateDoctorLogin(String email, String password) {
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) throw new InvalidCredentialsException("Invalid email or password");

        String stored = doctor.getPassword();

        if (passwordEncoder.matches(password, stored)) {
            // already BCrypt-hashed
        } else if (password.equals(stored)) {
            doctor.setPassword(passwordEncoder.encode(password));
            doctorRepository.save(doctor);
        } else {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return tokenService.generateToken(doctor.getEmail());
    }
}