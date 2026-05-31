package com.project.back_end.appointment.internal;

import com.project.back_end.appointment.Appointment;
import com.project.back_end.appointment.AppointmentDTO;
import com.project.back_end.appointment.AppointmentService;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.shared.Service;
import com.project.back_end.shared.ValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;
    private final ValidationService validationService;


    public AppointmentController(AppointmentService appointmentService, Service service, ValidationService validationService) {
        this.appointmentService = appointmentService;
        this.service = service;
        this.validationService = validationService;
    }

    @GetMapping("/{date}/{patientName}/")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @RequestHeader("Authorization") String authHeader) {

        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "doctor");
        List<AppointmentDTO> appointments = appointmentService.getAppointments(date, patientName, token);
        return ResponseEntity.ok(Map.of("appointments", appointments));
    }



    @PostMapping("/")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestBody Appointment appointment,
            @RequestHeader("Authorization") String authHeader) {

        String token = service.extractToken(authHeader);
        validationService.validateToken(token,"patient");

        if (appointment.getDoctor() == null || appointment.getAppointmentTime() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Doctor and appointment time are required"));
        }

        ServiceResult validationResult = service.validateAppointment(appointment.getDoctor().getId(), appointment.getAppointmentTime());
        if (validationResult == ServiceResult.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Doctor ID is invalid"));
        } else if (validationResult == ServiceResult.CONFLICT) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "The slot is already taken"));
        }

        ServiceResult bookResult = appointmentService.bookAppointment(appointment);
        if (bookResult == ServiceResult.SUCCESS) {
            return ResponseEntity.ok(Map.of("message", "Appointment booked successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to book appointment"));
        }
    }

    @PutMapping("/")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @RequestBody Appointment appointment,
            @RequestHeader("Authorization") String authHeader
    ){
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "patient");

        ServiceResult updateResult = appointmentService.updateAppointment(appointment, token);
        return switch (updateResult) {
            case SUCCESS -> ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "Appointment updated"));
            case DUPLICATE -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "The slot is already taken"));
            case CONFLICT ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "The doctor is unavailable at this time"));
            case NOT_FOUND ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The appointment does not exist"));
            case UNAUTHORIZED ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access to this appointment"));
        };

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @RequestHeader("Authorization") String authHeader
    ){
        String token = service.extractToken(authHeader);
        validationService.validateToken(token, "patient");

        ServiceResult cancelResult = appointmentService.cancelAppointment(id, token);
        return switch (cancelResult) {
            case SUCCESS -> ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "Appointment deleted successfully"));
            case DUPLICATE -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "The slot is already taken"));
            case NOT_FOUND ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The appointment does not exist"));
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Failed to delete appointment"));
            case UNAUTHORIZED ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access to this appointment"));
        };
    }

}
