package com.project.back_end.mvc;

import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    private final Service service;

    public DashboardController(Service service) {
        this.service = service;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "admin");
        if (validation.getStatusCode().is2xxSuccessful()) {
            return "admin/adminDashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "doctor");
        assert validation.getBody() != null;
        if (validation.getStatusCode().is2xxSuccessful()) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/loggedPatientDashboard/{token}")
    public String patientDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (validation.getStatusCode().is2xxSuccessful()) {
            return "loggedPatient/loggedPatientDashboard";
        }
        return "redirect:/";
    }

}