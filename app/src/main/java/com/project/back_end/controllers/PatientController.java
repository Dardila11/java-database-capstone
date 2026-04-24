package com.project.back_end.controllers;

import com.project.back_end.DTO.AuthDTO;
import com.project.back_end.exceptions.InvalidCredentialsException;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import com.project.back_end.services.ValidationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;
    private final ValidationService validationService;

    public PatientController(PatientService patientService, Service service, ValidationService validationService) {
        this.patientService = patientService;
        this.service = service;
        this.validationService = validationService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        validationService.validateToken(token, "patient"); // throws exception if not valid
        return ResponseEntity.ok(Map.of("patient", patientService.getPatientDetails(token)));
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        if (!service.validatePatient(patient.getEmail(), patient.getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Patient with this email or phone already exists"));
        }

        if (patientService.createPatient(patient) == 1) {
            return ResponseEntity.ok(Map.of("message", "Patient created successfully"));
        } else {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create patient"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthDTO.LoginRequest loginRequest){
        String token = validationService.validatePatientLogin(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(@PathVariable long id, @PathVariable String token) {
        validationService.validateToken(token, "patient"); // throws exception if not valid
        return ResponseEntity.ok(Map.of("appointments", patientService.getPatientAppointments(id)));
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if((tokenValidation == null) || (tokenValidation.getBody() == null)){
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("error")));
        }

        return service.filterPatient(token, condition, name);
    }
}
