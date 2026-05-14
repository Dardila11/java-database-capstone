package com.project.back_end.mvc;

import com.project.back_end.services.Service;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    private final Service service;

    public DashboardController(Service service) {
        this.service = service;
    }

    @GetMapping("/adminDashboard")
    public String adminDashboard(@RequestParam String token) {
        return service.validateToken(token, "admin") ? "admin/adminDashboard" : "redirect:/";
    }

    @GetMapping("/doctorDashboard")
    public String doctorDashboard(@RequestParam String token) {
        return service.validateToken(token, "doctor") ? "doctor/doctorDashboard" : "redirect:/";
    }

    @GetMapping("/loggedPatientDashboard")
    public String patientDashboard(@RequestParam String token) {
        return service.validateToken(token, "patient") ? "loggedPatient/loggedPatientDashboard" : "redirect:/";
    }

}