package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final TokenService tokenService;

  public AppointmentService(
      AppointmentRepository appointmentRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      TokenService tokenService) {
    this.appointmentRepository = appointmentRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.tokenService = tokenService;
  }

  // Saves a new appointment; returns 1 on success, 0 on failure.
  @Transactional
  public int bookAppointment(Appointment appointment) {
    try {
      appointmentRepository.save(appointment);
      return 1;
    } catch (Exception e) {
      return 0;
    }
  }

  // Validates patient ownership, appointment status, and doctor availability
  // before persisting the updated appointment time.
  // Returns: 1 = success, 0 = conflict/unavailable, -1 = not found, -2 = unauthorized.
  @Transactional
  public int updateAppointment(Appointment appointment, String token) {
    try {
      Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
      if (existingOpt.isEmpty()) {
        return -1;
      }
      Appointment existing = existingOpt.get();

      String email = tokenService.extractEmail(token);
      Patient patient = patientRepository.findByEmail(email);
      if (patient == null || !patient.getId().equals(existing.getPatient().getId())) {
        return -2;
      }

      if (existing.getStatus() != 0) {
        return 0;
      }

      Doctor doctor = existing.getDoctor();
      LocalTime requestedTime = appointment.getAppointmentTime().toLocalTime();
      String timeStr = String.format("%02d:%02d", requestedTime.getHour(), requestedTime.getMinute());

      boolean doctorAvailable = doctor.getAvailableTimes() != null &&
          doctor.getAvailableTimes().stream().anyMatch(slot -> slot.startsWith(timeStr));

      if (!doctorAvailable) {
        return 0;
      }

      existing.setAppointmentTime(appointment.getAppointmentTime());
      appointmentRepository.save(existing);
      return 1;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return 0;
    }
  }

  // Verifies the token belongs to the appointment's patient before deleting.
  // Returns: 1 = success, 0 = failure, -1 = not found, -2 = unauthorized.
  @Transactional
  public int cancelAppointment(long appointmentId, String token) {
    try {
      Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
      if (appointmentOpt.isEmpty()) {
        return -1;
      }
      Appointment appointment = appointmentOpt.get();

      String email = tokenService.extractEmail(token);
      Patient patient = patientRepository.findByEmail(email);
      if (patient == null || !patient.getId().equals(appointment.getPatient().getId())) {
        return -2;
      }

      appointmentRepository.deleteById(appointmentId);
      return 1;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return 0;
    }
  }

  // Returns all appointments for the doctor identified by token on the given date,
  // optionally narrowed by patient name.
  @Transactional
  public List<AppointmentDTO> getAppointments(String date, String patientName, String token) {
    String email = tokenService.extractEmail(token);
    Doctor doctor = doctorRepository.findByEmail(email);
    Long doctorId = doctor.getId();

    LocalDate localDate = LocalDate.parse(date);
    LocalDateTime start = localDate.atStartOfDay();
    LocalDateTime end = localDate.atTime(LocalTime.MAX);

    List<Appointment> appointments;
    if (patientName.equals("null")) {
      appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
    } else {
      appointments = appointmentRepository
          .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
              doctorId, patientName, start, end);
    }

    return appointments.stream().map(AppointmentDTO::from).collect(Collectors.toList());
  }

  @Transactional
  public void changeStatus(int status, long id) {
    appointmentRepository.updateStatus(status, id);
  }
}
