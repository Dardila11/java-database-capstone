package com.project.back_end.controllers;

import com.project.back_end.enums.ServiceResult;
import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import com.project.back_end.services.ValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/prescription")
public class PrescriptionController {
    
    private final PrescriptionService prescriptionService;
    private final AppointmentService appointmentService;
    private final ValidationService validationService;
    private final Service service;

    public PrescriptionController(PrescriptionService prescriptionService, AppointmentService appointmentService, ValidationService validationService, Service service) {
        this.prescriptionService = prescriptionService;
        this.appointmentService = appointmentService;
        this.validationService = validationService;
        this.service = service;
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, String>> savePrescription(
            @RequestBody Prescription prescription,
            @RequestHeader("Authorization") String authHeader
    ){
        String token = service.extractToken(authHeader);
        validationService.validateToken(token,"doctor");

        ServiceResult res = prescriptionService.savePrescription(prescription);

        return switch (res) {
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "prescription already exists"));
            case SUCCESS -> {
                appointmentService.changeStatus(1, prescription.getAppointmentId());
                yield ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "prescription saved successfully"));
            }
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "failed to save prescription"));
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Server error"));
        };

    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "doctor");
        return ResponseEntity.ok(Map.of("prescription", prescriptionService.getPrescription(appointmentId)));
    }
}
