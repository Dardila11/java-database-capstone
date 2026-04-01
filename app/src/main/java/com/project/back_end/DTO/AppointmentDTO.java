package com.project.back_end.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.project.back_end.models.Appointment;

/**
 * Flattened appointment view for API responses: avoids entity graphs and excludes
 * doctor/patient passwords. Derived fields mirror {@link Appointment} transients.
 */
public final class AppointmentDTO {

  private final Long id;
  private final Long doctorId;
  private final String doctorName;
  private final Long patientId;
  private final String patientName;
  private final String patientEmail;
  private final String patientPhone;
  private final String patientAddress;
  private final LocalDateTime appointmentTime;
  private final int status;

  public AppointmentDTO(
      Long id,
      Long doctorId,
      String doctorName,
      Long patientId,
      String patientName,
      String patientEmail,
      String patientPhone,
      String patientAddress,
      LocalDateTime appointmentTime,
      int status) {
    this.id = id;
    this.doctorId = doctorId;
    this.doctorName = doctorName;
    this.patientId = patientId;
    this.patientName = patientName;
    this.patientEmail = patientEmail;
    this.patientPhone = patientPhone;
    this.patientAddress = patientAddress;
    this.appointmentTime = appointmentTime;
    this.status = status;
  }

  public static AppointmentDTO from(Appointment appointment) {
    if (appointment == null) {
      return null;
    }
    var doctor = appointment.getDoctor();
    var patient = appointment.getPatient();
    return new AppointmentDTO(
        appointment.getId(),
        doctor != null ? doctor.getId() : null,
        doctor != null ? doctor.getName() : null,
        patient != null ? patient.getId() : null,
        patient != null ? patient.getName() : null,
        patient != null ? patient.getEmail() : null,
        patient != null ? patient.getPhone() : null,
        patient != null ? patient.getAddress() : null,
        appointment.getAppointmentTime(),
        appointment.getStatus());
  }

  public Long getId() {
    return id;
  }

  public Long getDoctorId() {
    return doctorId;
  }

  public String getDoctorName() {
    return doctorName;
  }

  public Long getPatientId() {
    return patientId;
  }

  public String getPatientName() {
    return patientName;
  }

  public String getPatientEmail() {
    return patientEmail;
  }

  public String getPatientPhone() {
    return patientPhone;
  }

  public String getPatientAddress() {
    return patientAddress;
  }

  public LocalDateTime getAppointmentTime() {
    return appointmentTime;
  }

  public int getStatus() {
    return status;
  }

  public LocalDate getAppointmentDate() {
    return appointmentTime != null ? appointmentTime.toLocalDate() : null;
  }

  public LocalTime getAppointmentTimeOnly() {
    return appointmentTime != null ? appointmentTime.toLocalTime() : null;
  }

  public LocalDateTime getEndTime() {
    return appointmentTime != null ? appointmentTime.plusHours(1) : null;
  }
}
