package com.project.back_end.doctor;

import com.project.back_end.shared.AuthDTO;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.shared.Service;
import com.project.back_end.shared.ValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;
    private final ValidationService validationService;

    public DoctorController(DoctorService doctorService, Service service, ValidationService validationService) {
        this.doctorService = doctorService;
        this.service = service;
        this.validationService = validationService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> doctorLogin(@RequestBody AuthDTO.LoginRequest loginRequest){
        String token = validationService.validateDoctorLogin(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor(){
        return ResponseEntity.ok(Map.of("doctors", doctorService.getDoctors()));

    }

    @GetMapping("/availability/{user}/{doctorId}/{date}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable long doctorId,
            @PathVariable LocalDateTime date,
            @RequestHeader("Authorization") String authHeader
    ){
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, user);
        return ResponseEntity.ok(Map.of("availability", doctorService.getDoctorAvailability(doctorId, date)));
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @RequestBody Doctor doctor,
            @RequestHeader("Authorization") String authHeader
    ){
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "admin");
        ServiceResult saveResult = doctorService.saveDoctor(doctor);
        return switch (saveResult) {
            case SUCCESS -> ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "doctor saved successfully"));
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "failed to save doctor"));
            case DUPLICATE -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "doctor already exists"));
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "unauthorized"));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "doctor not found"));
        };
    }

    @PutMapping("/")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @RequestHeader("Authorization") String authHeader
    ){
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "admin");
        ServiceResult updateResult = doctorService.updateDoctor(doctor);
        return switch (updateResult) {
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error",
                    "doctor does not exist"));
            case SUCCESS -> ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "doctor updated"));
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "failed to update"));
            default ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "failed to update"));
            };
        }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "admin");
        ServiceResult deleteResult = doctorService.deleteDoctor(id);
        return switch (deleteResult) {
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "doctor does not exist"));
            case SUCCESS -> ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "doctor deleted successfully"));
            default ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "failed to delete doctor"));
        };
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality
    ) {
        return ResponseEntity.ok(Map.of("doctors", service.filterDoctor(name,speciality, time)));
    }

}
