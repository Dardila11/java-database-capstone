package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.project.back_end.enums.ServiceResult;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

@org.springframework.stereotype.Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository,
                          TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
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

    @Transactional
    public List<AppointmentDTO> getPatientAppointments(Long patientId){
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(AppointmentDTO::from)
                .collect(Collectors.toList());
    }

    public ResponseEntity<Map<String, Object>> filterByCondition(long patientId, String condition) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else {
                response.put("message", "Invalid condition: use 'past' or 'future'");
                return ResponseEntity.badRequest().body(response);
            }
            List<AppointmentDTO> appointments = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status)
                    .stream()
                    .map(AppointmentDTO::from)
                    .collect(Collectors.toList());
            response.put("appointments", appointments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByDoctor(String doctorName, long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<AppointmentDTO> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientId(doctorName, patientId)
                    .stream()
                    .map(AppointmentDTO::from)
                    .collect(Collectors.toList());
            response.put("appointments", appointments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String doctorName, long patientId, String condition) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else {
                response.put("message", "Invalid condition: use 'past' or 'future'");
                return ResponseEntity.badRequest().body(response);
            }
            List<AppointmentDTO> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status)
                    .stream()
                    .map(AppointmentDTO::from)
                    .collect(Collectors.toList());
            response.put("appointments", appointments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    public Patient getPatientDetails(String token) {
        String email = tokenService.extractEmail(token);
        if (email == null) throw new InvalidTokenException("Invalid token");
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
    }
}