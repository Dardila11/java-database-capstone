
package com.project.back_end.controllers;

import com.project.back_end.DTO.AuthDTO;
import com.project.back_end.services.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {
    private final ValidationService validationService;

    public AdminController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody AuthDTO.AdminLoginRequest adminLoginRequest) {
        String token = validationService.validateAdminLogin(adminLoginRequest.username(), adminLoginRequest.password());
        return ResponseEntity.ok(Map.of("token", token));
    }
}

