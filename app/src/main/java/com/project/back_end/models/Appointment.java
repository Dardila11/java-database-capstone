package com.project.back_end.models;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

@Entity
(name = "appointments")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  // indicates many appointments can be linked to one doctor.
  @NotNull(message = "Doctor is required")
  private Doctor doctor;

  @ManyToOne
  // indicates many appointments can be linked to one patient.
  @NotNull(message = "Patient is required")
  private Patient patient;
  @Future(message = "Appointment time must be in the future")
  @NotNull(message = "Appointment time is required")
  private LocalDateTime appointmentTime;
  @NotNull(message = "Status is required")
  // 0 means the appointment is scheduled.
  // 1 means the appointment has been completed.
  // 2 means the appointment has been cancelled.
  private int status;

  @Transient
  private LocalDateTime getEndTime() {
    return appointmentTime.plusHours(1);
  }

  @Transient
  // extracts only the date part from the appointmentTime field.
  private LocalDate getAppointmenDate() {
    return appointmentTime.toLocalDate();
  }
  @Transient
  // extracts only the time part from the appointmentTime field.
  private LocalTime getAppointmentTimeOnly() {
    return appointmentTime.toLocalTime();
  }

  // constructor
  public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status) {
    this.doctor = doctor;
    this.patient = patient;
    this.appointmentTime = appointmentTime;
    this.status = status;
  }

  // getters
  public Long getId() {
    return id;
  }
  public Doctor getDoctor() {
    return doctor;
  }
  public Patient getPatient() {
    return patient;
  }
  public LocalDateTime getAppointmentTime() {
    return appointmentTime;
  }
  public int getStatus() {
    return status;
  }

  //setters
  public void setId(Long id) {
    this.id = id;
  }
  public void setDoctor(Doctor doctor) {
    this.doctor = doctor;
  }
  public void setPatient(Patient patient) {
    this.patient = patient;
  }
  public void setAppointmentTime(LocalDateTime appointmentTime) {
    this.appointmentTime = appointmentTime;
  }
  public void setStatus(int status) {
    this.status = status;
  }
}

