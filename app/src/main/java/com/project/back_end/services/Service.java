package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService, DoctorRepository doctorRepository, PatientRepository patientRepository, DoctorService doctorService, PatientService patientService) {
        this.tokenService = tokenService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }


    public ResponseEntity<Map<String, String>> validateToken(String token, String role) {
        boolean valid = tokenService.validateToken(token, role);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        }
        return ResponseEntity.ok(Map.of("message", "Token is valid"));
    }

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.
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

// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.
    public int validateAppointment(long doctorId, LocalDateTime appointmentTime) {
        if (!doctorRepository.existsById(doctorId)) {
            return -1;
        }
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, appointmentTime);
        String requestedStart = String.format("%02d:%02d", appointmentTime.getHour(), appointmentTime.getMinute());
        boolean matched = availableSlots.stream()
                .anyMatch(slot -> slot.startsWith(requestedStart));
        return matched ? 1 : 0;
    }

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.
    public boolean validatePatient(String email, String phone) {
        return patientRepository.findByEmailOrPhone(email, phone) == null;
    }

// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and Doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.
    public ResponseEntity<Map<String, Object>> filterPatient(String token, String condition, String doctorName) {
        String email = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(email);
        long patientId = patient.getId();

        boolean hasCondition = condition != null && !condition.isEmpty();
        boolean hasDoctorName = doctorName != null && !doctorName.isEmpty();

        if (hasCondition && hasDoctorName) {
            return patientService.filterByDoctorAndCondition(doctorName, patientId, condition);
        } else if (hasCondition) {
            return patientService.filterByCondition(patientId, condition);
        } else if (hasDoctorName) {
            return patientService.filterByDoctor(doctorName, patientId);
        } else {
            return patientService.getPatientAppointment(patientId);
        }
    }
}
