package com.project.back_end.mvc;

import com.project.back_end.services.TokenService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final TokenService tokenService;

    public DashboardController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        if (tokenService.validateToken(token, "admin")) {
            return "admin/adminDashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        if (tokenService.validateToken(token, "doctor")) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/";
    }
}
