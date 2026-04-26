package com.project.back_end.controllers;

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

    public PrescriptionController(PrescriptionService prescriptionService, AppointmentService appointmentService, ValidationService validationService) {
        this.prescriptionService = prescriptionService;
        this.appointmentService = appointmentService;
        this.validationService = validationService;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @RequestBody Prescription prescription,
            @PathVariable String token
    ){
        validationService.validateToken(token,"doctor");

        int res = prescriptionService.savePrescription(prescription);

        return switch (res) {
            case -1 -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "prescription already exists"));
            case 1 -> {
                appointmentService.changeStatus(1, prescription.getAppointmentId());
                yield ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "prescription saved successfully"));
            }
            case 0 -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "failed to save prescription"));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Server error"));
        };

    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token
    ) {
        validationService.validateToken(token, "doctor");
        return ResponseEntity.ok(Map.of("prescription", prescriptionService.getPrescription(appointmentId)));
    }
}
