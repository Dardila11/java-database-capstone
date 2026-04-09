package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {
    
    private final PrescriptionService prescriptionService;
    private final Service service;
    private final AppointmentService appointmentService;

    public PrescriptionController(PrescriptionService prescriptionService, Service service, AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @RequestBody Prescription prescription,
            @PathVariable String token
    ){
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation != null) {
            assert tokenValidation.getBody() != null;
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("error")));
        }

        ResponseEntity<Map<String, String>> response = prescriptionService.savePrescription(prescription);
        if (response.getStatusCode().is2xxSuccessful()) {
            appointmentService.changeStatus(1, prescription.getAppointmentId());
        }
        return response;
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation != null) {
            assert tokenValidation.getBody() != null;
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("error")));
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
