package com.project.back_end.patient.internal;

import com.project.back_end.appointment.AppointmentService;
import com.project.back_end.patient.Patient;
import com.project.back_end.shared.AuthDTO;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.shared.Service;
import com.project.back_end.shared.ValidationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final Service service;
    private final ValidationService validationService;

    public PatientController(PatientService patientService, AppointmentService appointmentService,
                             Service service, ValidationService validationService) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.service = service;
        this.validationService = validationService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getPatient(@RequestHeader("Authorization") String authHeader) {
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "patient"); // throws exception if not valid
        return ResponseEntity.ok(Map.of("patient", patientService.getPatientDetails(token)));
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        if (!service.validatePatient(patient.getEmail(), patient.getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Patient with this email or phone already exists"));
        }

        if (patientService.createPatient(patient) == ServiceResult.SUCCESS) {
            return ResponseEntity.ok(Map.of("message", "Patient created successfully"));
        } else {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create patient"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthDTO.LoginRequest loginRequest){
        String token = validationService.validatePatientLogin(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(
            @PathVariable long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "patient"); // throws exception if not valid
        return ResponseEntity.ok(Map.of("appointments", appointmentService.getPatientAppointments(id)));
    }

    @GetMapping("/filter/{condition}/{name}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @RequestHeader("Authorization") String authHeader) {

        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "patient"); // throws exception if not valid
        return ResponseEntity.ok(Map.of("appointments", service.filterPatient(token, condition, name)));
    }
}
