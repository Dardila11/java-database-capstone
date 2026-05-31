package com.project.back_end.shared;

import com.project.back_end.appointment.AppointmentDTO;
import com.project.back_end.appointment.AppointmentService;
import com.project.back_end.enums.ServiceResult;
import com.project.back_end.exceptions.NotFoundException;
import com.project.back_end.doctor.Doctor;
import com.project.back_end.doctor.DoctorRepository;
import com.project.back_end.doctor.DoctorService;
import com.project.back_end.patient.Patient;
import com.project.back_end.patient.PatientLookup;
import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private final PatientLookup patientLookup;
    private final AppointmentService appointmentService;

    public Service(TokenService tokenService, DoctorRepository doctorRepository,
                   DoctorService doctorService, PatientLookup patientLookup,
                   AppointmentService appointmentService) {
        this.tokenService = tokenService;
        this.doctorRepository = doctorRepository;
        this.doctorService = doctorService;
        this.patientLookup = patientLookup;
        this.appointmentService = appointmentService;
    }

    /**
     * Strips the {@code Bearer } prefix from an Authorization header value.
     *
     * @param authHeader the raw Authorization header (may be {@code null})
     * @return the bare JWT string, or the original value if the prefix is absent
     */
    public String extractToken(String authHeader) {
        return (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : authHeader;
    }

    /**
     * Returns whether the given JWT is valid for the specified role.
     * Used by Thymeleaf dashboard controllers to gate page access without throwing exceptions.
     *
     * @param token the JWT to validate
     * @param role  the required role (e.g. {@code "patient"}, {@code "doctor"})
     * @return {@code true} if the token is valid and matches the role, {@code false} otherwise
     */
    public boolean validateToken(String token, String role) {
        return tokenService.validateToken(token, role);
    }

    /**
     * Returns doctors matching any combination of name, speciality, and available time slot.
     * Falls back to all doctors when no filters are supplied.
     *
     * @param name      partial or full doctor name (nullable/empty to skip)
     * @param speciality medical speciality (nullable/empty to skip)
     * @param time      desired time slot in {@code HH:mm} format (nullable/empty to skip)
     * @return filtered (and optionally time-narrowed) list of {@link Doctor} records
     */
    public List<Doctor> filterDoctor(String name, String speciality, String time) {
        boolean hasName = name != null && !name.isEmpty();
        boolean hasSpeciality = speciality != null && !speciality.isEmpty();
        boolean hasTime = time != null && !time.isEmpty();

        List<Doctor> doctors;
        if (hasName && hasSpeciality) {
            doctors = doctorService.filterDoctorByNameAndSpeciality(name, speciality);
        } else if (hasName) {
            doctors = doctorService.findDoctorByName(name);
        } else if (hasSpeciality) {
            doctors = doctorService.filterDoctorBySpeciality(speciality);
        } else {
            doctors = doctorService.getDoctors();
        }

        return hasTime ? doctorService.filterDoctorByTime(doctors, time) : doctors;
    }

    /**
     * Checks whether a requested appointment time is available for a doctor.
     *
     * @param doctorId        the ID of the doctor
     * @param appointmentTime the requested date and time
     * @return {@link ServiceResult#SUCCESS} if the slot is free,
     *         {@link ServiceResult#CONFLICT} if the slot is already taken,
     *         {@link ServiceResult#NOT_FOUND} if the doctor does not exist
     */
    public ServiceResult validateAppointment(long doctorId, LocalDateTime appointmentTime) {
        if (!doctorRepository.existsById(doctorId)) {
            return ServiceResult.NOT_FOUND;
        }
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, appointmentTime);
        String requestedStart = String.format("%02d:%02d", appointmentTime.getHour(), appointmentTime.getMinute());
        boolean matched = availableSlots.stream()
                .anyMatch(slot -> slot.startsWith(requestedStart));
        return matched ? ServiceResult.SUCCESS : ServiceResult.CONFLICT;
    }

    /**
     * Returns {@code true} when a patient with the given email or phone exists,
     * enforcing uniqueness before registration.
     *
     * @param email the patient email address
     * @param phone the patient phone number
     * @return {@code true} if the combination is unique, {@code false} if a duplicate is found
     */
    public boolean validatePatient(String email, String phone) {
        return !patientLookup.existsByEmailOrPhone(email, phone);
    }

    /**
     * Returns the authenticated patient's appointments, optionally filtered by condition and/or doctor name.
     *
     * @param token      the patient's JWT (used to resolve their identity)
     * @param condition  {@code "past"} or {@code "future"} (nullable/empty to skip); throws
     *                   {@link IllegalArgumentException} for any other value
     * @param doctorName partial or full doctor name (nullable/empty to skip)
     * @return matching {@link AppointmentDTO} list; all appointments when no filters are supplied
     * @throws NotFoundException if the token's email does not match any patient
     */
    public List<AppointmentDTO> filterPatient(String token, String condition, String doctorName) {
        String email = tokenService.extractEmail(token);
        Patient patient = patientLookup.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Patient not found"));
        long patientId = patient.getId();

        boolean hasCondition = condition != null && !condition.isEmpty();
        boolean hasDoctorName = doctorName != null && !doctorName.isEmpty();

        if (hasCondition && hasDoctorName) {
            return appointmentService.filterByDoctorAndCondition(doctorName, patientId, condition);
        } else if (hasCondition) {
            return appointmentService.filterByCondition(patientId, condition);
        } else if (hasDoctorName) {
            return appointmentService.filterByDoctor(doctorName, patientId);
        } else {
            return appointmentService.getPatientAppointments(patientId);
        }
    }
}
