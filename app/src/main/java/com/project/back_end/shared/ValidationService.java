package com.project.back_end.shared;

import com.project.back_end.exceptions.InvalidCredentialsException;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.admin.Admin;
import com.project.back_end.admin.AdminRepository;
import com.project.back_end.doctor.Doctor;
import com.project.back_end.doctor.DoctorRepository;
import com.project.back_end.patient.Patient;
import com.project.back_end.patient.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles authentication validation for all user roles (patient, admin, doctor)
 * and JWT token assertion. Throws typed exceptions caught by {@link com.project.back_end.exceptions.GlobalExceptionHandler}
 * rather than returning status codes.
 */
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

    /**
     * Asserts that a JWT token is valid for the given role.
     * Role validation hits the database on every call.
     *
     * @param token the Bearer token from the {@code Authorization} header
     * @param role  the expected role (e.g. {@code "patient"}, {@code "admin"}, {@code "doctor"})
     * @throws com.project.back_end.exceptions.InvalidTokenException if the token is missing, expired, or does not match the role
     */
    public void validateToken(String token, String role) {
        boolean isValid = tokenService.validateToken(token, role);
        if(!isValid) throw new InvalidTokenException("Invalid token");
    }

    /**
     * Validates patient credentials and returns a JWT on success.
     * If the stored password is plain-text (legacy record), it is migrated to BCrypt
     * on the first successful login.
     *
     * @param email    the patient's email address
     * @param password the raw password supplied by the patient
     * @return a signed JWT whose subject is the patient's email
     * @throws com.project.back_end.exceptions.InvalidCredentialsException if no patient with the given email exists or the password does not match
     */
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

    /**
     * Validates admin credentials and returns a JWT on success.
     * If the stored password is plain-text (legacy record), it is migrated to BCrypt
     * on the first successful login.
     *
     * @param username the admin's username
     * @param password the raw password supplied by the admin
     * @return a signed JWT whose subject is the admin's username
     * @throws com.project.back_end.exceptions.InvalidCredentialsException if no admin with the given username exists or the password does not match
     */
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

    /**
     * Validates doctor credentials and returns a JWT on success.
     * If the stored password is plain-text (legacy record), it is migrated to BCrypt
     * on the first successful login.
     *
     * @param email    the doctor's email address
     * @param password the raw password supplied by the doctor
     * @return a signed JWT whose subject is the doctor's email
     * @throws com.project.back_end.exceptions.InvalidCredentialsException if no doctor with the given email exists or the password does not match
     */
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