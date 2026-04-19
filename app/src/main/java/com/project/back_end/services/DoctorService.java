package com.project.back_end.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

import jakarta.transaction.Transactional;

@Service
public class DoctorService {
  private final DoctorRepository doctorRepository;
  private final AppointmentRepository appointmentRepository;
  private final TokenService tokenService;

  public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository, TokenService tokenService) {
    this.doctorRepository = doctorRepository;
    this.appointmentRepository = appointmentRepository;
    this.tokenService = tokenService;
  }

  @Transactional
  public List<String> getDoctorAvailability(long doctorId, LocalDateTime date) {
    LocalDateTime start = date.toLocalDate().atStartOfDay();
    LocalDateTime end = date.toLocalDate().atTime(23, 59, 59);

    List<Appointment> appointments = appointmentRepository
        .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

    List<String> bookedSlots = appointments.stream()
        .map(a -> String.format("%02d:%02d-%02d:%02d",
            a.getAppointmentTime().getHour(),
            a.getAppointmentTime().getMinute(),
            a.getAppointmentTime().plusHours(1).getHour(),
            a.getAppointmentTime().plusHours(1).getMinute()))
        .toList();

    Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
    if (doctor == null) return new ArrayList<>();

    return doctor.getAvailableTimes().stream()
        .filter(slot -> !bookedSlots.contains(slot))
        .collect(Collectors.toList());
  }

  public int saveDoctor(Doctor doctor) {
    try {
      if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
        return -1;
      }
      doctorRepository.save(doctor);
      return 1;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return 0;
    }
  }

  public int updateDoctor(Doctor doctor) {
    try {
      if (!doctorRepository.existsById(doctor.getId())) {
        return -1;
      }
      doctorRepository.save(doctor);
      return 1;
    } catch (Exception e) {
      return 0;
    }
  }

  @Transactional
  public List<Doctor> getDoctors() {
    List<Doctor> doctors = doctorRepository.findAll();
    doctors.forEach(d -> d.getAvailableTimes().size());
    return doctors;
  }

  public int deleteDoctor(long doctorId) {
    try {
      if (!doctorRepository.existsById(doctorId)) {
        return -1;
      }
      appointmentRepository.deleteAllByDoctorId(doctorId);
      doctorRepository.deleteById(doctorId);
      return 1;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return 0;
    }
  }

  public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
    Map<String, String> response = new HashMap<>();

    try {
      Doctor doctor = doctorRepository.findByEmail(login.getEmail());

      if (doctor == null || !doctor.getPassword().equals(login.getPassword())) {
        response.put("message", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      String token = tokenService.generateToken(doctor.getEmail());
      response.put("token", token);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Transactional
  public List<Doctor> findDoctorByName(String name) {
    List<Doctor> doctors = doctorRepository.findByNameLike(name);
    doctors.forEach(d -> d.getAvailableTimes().size());
    return doctors;
  }

  @Transactional
  public List<Doctor> filterDoctorsByNameSpecialityAndTime(String name, String speciality, String time) {
    List<Doctor> doctors = doctorRepository
        .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, speciality);
    doctors.forEach(d -> d.getAvailableTimes().size());
    return filterDoctorByTime(doctors, time);
  }

  public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String time) {
    return doctors.stream()
        .filter(doctor -> doctor.getAvailableTimes().stream().anyMatch(slot -> {
          int hour = Integer.parseInt(slot.split("-")[0].split(":")[0]);
          return "AM".equalsIgnoreCase(time) ? hour < 12 : hour >= 12;
        }))
        .collect(Collectors.toList());
  }

  @Transactional
  public List<Doctor> filterDoctorByNameAndTime(String name, String time) {
    List<Doctor> doctors = doctorRepository.findByNameLike(name);
    doctors.forEach(d -> d.getAvailableTimes().size());
    return filterDoctorByTime(doctors, time);
  }

  public List<Doctor> filterDoctorByNameAndSpeciality(String name, String speciality) {
    return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, speciality);
  }

  @Transactional
  public List<Doctor> filterDoctorByTimeAndSpeciality(String speciality, String time) {
    List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(speciality);
    doctors.forEach(d -> d.getAvailableTimes().size());
    return filterDoctorByTime(doctors, time);
  }

  public List<Doctor> filterDoctorBySpeciality(String speciality) {
    return doctorRepository.findBySpecialtyIgnoreCase(speciality);
  }

  @Transactional
  public List<Doctor> filterDoctorsByTime(String time) {
    List<Doctor> doctors = doctorRepository.findAll();
    doctors.forEach(d -> d.getAvailableTimes().size());
    return filterDoctorByTime(doctors, time);
  }
}