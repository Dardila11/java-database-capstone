package com.project.back_end.controllers;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import com.project.back_end.services.ValidationService;
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

// 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.

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



// 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.
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


// 5. Define the `updateAppointment` Method:
//    - Handles HTTP PUT requests to modify an existing appointment.
//    - Accepts a validated `Appointment` object and a token as input.
//    - Validates the token for `"patient"` role.
//    - Delegates the update logic to the `AppointmentService`.
//    - Returns an appropriate success or failure response based on the update result.
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


// 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.
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
