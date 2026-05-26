package com.project.back_end.patient;

import java.util.List;
import java.util.stream.Collectors;

import com.project.back_end.enums.ServiceResult;
import com.project.back_end.exceptions.InvalidTokenException;
import com.project.back_end.exceptions.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.appointment.AppointmentDTO;
import com.project.back_end.appointment.AppointmentRepository;
import com.project.back_end.shared.TokenService;

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

    public List<AppointmentDTO> filterByCondition(long patientId, String condition) {
        int status = parseConditionStatus(condition);
        return appointmentRepository
                .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status)
                .stream()
                .map(AppointmentDTO::from)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> filterByDoctor(String doctorName, long patientId) {
        return appointmentRepository
                .filterByDoctorNameAndPatientId(doctorName, patientId)
                .stream()
                .map(AppointmentDTO::from)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> filterByDoctorAndCondition(String doctorName, long patientId, String condition) {
        int status = parseConditionStatus(condition);
        return appointmentRepository
                .filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status)
                .stream()
                .map(AppointmentDTO::from)
                .collect(Collectors.toList());
    }

    private int parseConditionStatus(String condition) {
        if ("past".equalsIgnoreCase(condition)) return 1;
        if ("future".equalsIgnoreCase(condition)) return 0;
        throw new IllegalArgumentException("Invalid condition: use 'past' or 'future'");
    }


    public Patient getPatientDetails(String token) {
        String email = tokenService.extractEmail(token);
        if (email == null) throw new InvalidTokenException("Invalid token");
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
    }
}