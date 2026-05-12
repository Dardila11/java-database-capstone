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
import com.project.back_end.enums.ServiceResult;
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

  // Saves a new appointment; returns SUCCESS on success, CONFLICT on failure.
  @Transactional
  public ServiceResult bookAppointment(Appointment appointment) {
    try {
      appointmentRepository.save(appointment);
      return ServiceResult.SUCCESS;
    } catch (Exception e) {
      return ServiceResult.CONFLICT;
    }
  }

  @Transactional
  public ServiceResult updateAppointment(Appointment appointment, String token) {
    try {
      Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
      if (existingOpt.isEmpty()) {
        return ServiceResult.NOT_FOUND;
      }
      Appointment existing = existingOpt.get();

      String email = tokenService.extractEmail(token);
      Optional<Patient> patient = patientRepository.findByEmail(email);
      if (patient.isEmpty() || !patient.get().getId().equals(existing.getPatient().getId())) {
        return ServiceResult.UNAUTHORIZED;
      }

      if (existing.getStatus() != 0) {
        return ServiceResult.CONFLICT;
      }

      Doctor doctor = existing.getDoctor();
      LocalTime requestedTime = appointment.getAppointmentTime().toLocalTime();
      String timeStr = String.format("%02d:%02d", requestedTime.getHour(), requestedTime.getMinute());

      boolean doctorAvailable = doctor.getAvailableTimes() != null &&
          doctor.getAvailableTimes().stream().anyMatch(slot -> slot.startsWith(timeStr));

      if (!doctorAvailable) {
        return ServiceResult.CONFLICT;
      }

      existing.setAppointmentTime(appointment.getAppointmentTime());
      appointmentRepository.save(existing);
      return ServiceResult.SUCCESS;
    } catch (Exception e) {
      return ServiceResult.CONFLICT;
    }
  }

  @Transactional
  public ServiceResult cancelAppointment(long appointmentId, String token) {
    try {
      Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
      if (appointmentOpt.isEmpty()) {
        return ServiceResult.NOT_FOUND;
      }
      Appointment appointment = appointmentOpt.get();

      String email = tokenService.extractEmail(token);
      Optional<Patient> patient = patientRepository.findByEmail(email);
      if (patient.isEmpty() || !patient.get().getId().equals(appointment.getPatient().getId())) {
        return ServiceResult.UNAUTHORIZED;
      }

      appointmentRepository.deleteById(appointmentId);
      return ServiceResult.SUCCESS;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return ServiceResult.CONFLICT;
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
