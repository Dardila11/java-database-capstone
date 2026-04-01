package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            TokenService tokenService,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * Persists a new appointment. Returns 1 on success, 0 on failure.
     */
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Updates an existing appointment after verifying patient ownership and
     * doctor availability at the new requested time.
     */
    @Transactional
    public ResponseEntity<String> updateAppointment(Appointment appointment, String token) {
        try {
            Optional<Appointment> existing = appointmentRepository.findById(appointment.getId());
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
            }

            Appointment current = existing.get();

            String email = tokenService.extractEmail(token);
            var patient = patientRepository.findByEmail(email);
            if (patient == null || !current.getPatient().getId().equals(patient.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: patient mismatch");
            }

            if (current.getStatus() != 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Only scheduled appointments can be updated");
            }

            int availability = checkDoctorAvailability(
                    appointment.getDoctor().getId(),
                    appointment.getAppointmentTime());
            if (availability == -1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
            }
            if (availability == 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Doctor is not available at the requested time");
            }

            current.setDoctor(appointment.getDoctor());
            current.setAppointmentTime(appointment.getAppointmentTime());
            appointmentRepository.save(current);
            return ResponseEntity.ok("Appointment updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating appointment: " + e.getMessage());
        }
    }

    /**
     * Cancels (deletes) an appointment after verifying the requesting patient
     * owns it.
     */
    @Transactional
    public ResponseEntity<String> cancelAppointment(Long appointmentId, String token) {
        try {
            Optional<Appointment> existing = appointmentRepository.findById(appointmentId);
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
            }

            Appointment appointment = existing.get();

            String email = tokenService.extractEmail(token);
            var patient = patientRepository.findByEmail(email);
            if (patient == null || !appointment.getPatient().getId().equals(patient.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: patient mismatch");
            }

            appointmentRepository.deleteById(appointmentId);
            return ResponseEntity.ok("Appointment cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling appointment: " + e.getMessage());
        }
    }

    /**
     * Returns all appointments for the authenticated doctor on the given date,
     * optionally filtered by patient name.
     */
    @Transactional
    public ResponseEntity<List<AppointmentDTO>> getAppointments(
            LocalDate date, String patientName, String token) {
        try {
            String email = tokenService.extractEmail(token);
            Doctor doctor = doctorRepository.findByEmail(email);
            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);

            List<Appointment> appointments;
            if (patientName == null || patientName.isBlank()) {
                appointments = appointmentRepository
                        .findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);
            } else {
                appointments = appointmentRepository
                        .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                                doctor.getId(), patientName, start, end);
            }

            List<AppointmentDTO> dtos = appointments.stream()
                    .map(AppointmentDTO::from)
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the status field of an appointment (0 = scheduled, 1 = completed).
     */
    @Transactional
    public ResponseEntity<String> changeStatus(long appointmentId, int status) {
        try {
            if (!appointmentRepository.existsById(appointmentId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
            }
            appointmentRepository.updateStatus(status, appointmentId);
            return ResponseEntity.ok("Appointment status updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating status: " + e.getMessage());
        }
    }

    /**
     * Checks whether a doctor has the requested appointment time in their
     * available-times list.
     *
     * @return 1 if available, 0 if not available, -1 if doctor not found
     */
    private int checkDoctorAvailability(Long doctorId, LocalDateTime requestedTime) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return -1;
        }

        Doctor doctor = doctorOpt.get();
        List<String> availableTimes = doctor.getAvailableTimes();
        if (availableTimes == null || availableTimes.isEmpty()) {
            return 0;
        }

        String requestedHHmm = requestedTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        for (String slot : availableTimes) {
            String slotStart = slot.split("-")[0].trim();
            if (slotStart.equals(requestedHHmm)) {
                return 1;
            }
        }
        return 0;
    }
}
