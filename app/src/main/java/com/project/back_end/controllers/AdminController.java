
package com.project.back_end.controllers;

import com.project.back_end.DTO.AdminDTO;
import com.project.back_end.services.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to indicate that it's a REST controller, used to handle web requests and return JSON responses.
//    - Use `@RequestMapping("${api.path}admin")` to define a base path for all endpoints in this controller.
//    - This allows the use of an external property (`api.path`) for flexible configuration of endpoint paths.
@RestController
@RequestMapping("${api.path}admin")
public class AdminController {
    private final ValidationService validationService;

    public AdminController(ValidationService validationService) {
        this.validationService = validationService;
    }


// 3. Define the `adminLogin` Method:
//    - Handles HTTP POST requests for admin login functionality.
//    - Accepts an `Admin` object in the request body, which contains login credentials.
//    - Delegates authentication logic to the `validateAdmin` method in the service layer.
//    - Returns a `ResponseEntity` with a `Map` containing login status or messages.
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody AdminDTO adminDTO) {
        String token = validationService.validateAdminLogin(adminDTO.username(), adminDTO.password());
        return ResponseEntity.ok(Map.of("token", token));
    }




}

