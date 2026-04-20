package com.project.back_end.controllers;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.


// 2. Autowire Dependencies:
//    - Inject `AppointmentService` for handling the business logic specific to appointments.
//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.

    private final AppointmentService appointmentService;
    private final Service service;


    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

// 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {

        // validate token
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "user");
        if((tokenValidation == null) || (tokenValidation.getBody() == null)){
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("error")));
        }
        List<AppointmentDTO> appointments = appointmentService.getAppointments(date, patientName, token);
        return ResponseEntity.ok(Map.of("appointments", appointments));
    }



// 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {

        // validate token
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if((tokenValidation == null) || (tokenValidation.getBody() == null)){
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("error")));
        }

        if (appointment.getDoctor() == null || appointment.getAppointmentTime() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Doctor and appointment time are required"));
        }

        int validationResult = service.validateAppointment(appointment.getDoctor().getId(), appointment.getAppointmentTime());
        if (validationResult == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Doctor ID is invalid"));
        } else if (validationResult == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "The slot is already taken"));
        }

        int bookResult = appointmentService.bookAppointment(appointment);
        if (bookResult == 1) {
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
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token
    ){
        // validate token
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if((tokenValidation == null) || (tokenValidation.getBody() == null)){
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("error")));
        }

        int updateResult = appointmentService.updateAppointment(appointment, token);
        return switch (updateResult) {
            case 1 -> ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "Appointment updated"));
            case 0 ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "The doctor is unavailable at this time"));
            case -1 ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The appointment does not exist"));
            case -2 ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access to this appointment"));
            default ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update appointment"));
        };

    }


// 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token
    ){
        // validate token
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if((tokenValidation == null) || (tokenValidation.getBody() == null)){
            return ResponseEntity.status(tokenValidation.getStatusCode())
                    .body(Map.of("error", tokenValidation.getBody().get("error")));
        }

        // Returns: 1 = success, 0 = failure, -1 = not found, -2 = unauthorized.
        int cancelResult = appointmentService.cancelAppointment(id, token);
        return switch (cancelResult) {
            case 1 -> ResponseEntity.status(HttpStatus.OK).body(Map.of("success", "Appointment deleted successfully"));
            case -1 ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The appointment does not exist"));
            case 0 -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Failed to delete appointment"));
            case -2 ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access to this appointment"));

            default -> throw new IllegalStateException("Unexpected value: " + cancelResult);
        };
    }

}
