package com.project.back_end.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.project.back_end.enums.ServiceResult;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Transactional;
@Service
public class DoctorService {
  private final DoctorRepository doctorRepository;
  private final AppointmentRepository appointmentRepository;
  private final PasswordEncoder passwordEncoder;

  public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository,
                       PasswordEncoder passwordEncoder) {
    this.doctorRepository = doctorRepository;
    this.appointmentRepository = appointmentRepository;
    this.passwordEncoder = passwordEncoder;
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

  public ServiceResult saveDoctor(Doctor doctor) {
    try {
      if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
        return ServiceResult.DUPLICATE;
      }
      doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
      doctorRepository.save(doctor);
      return ServiceResult.SUCCESS;
    } catch (Exception e) {
      return ServiceResult.CONFLICT;
    }
  }

  public ServiceResult updateDoctor(Doctor doctor) {
    try {
      Doctor existing = doctorRepository.findById(doctor.getId()).orElse(null);
      if (existing == null) return ServiceResult.NOT_FOUND;
      if (doctor.getPassword() != null && !doctor.getPassword().isBlank()) {
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
      } else {
        // preserve the existing hash when no new password is provided
        doctor.setPassword(existing.getPassword());
      }
      doctorRepository.save(doctor);
      return ServiceResult.SUCCESS;
    } catch (Exception e) {
      return ServiceResult.CONFLICT;
    }
  }

  @Transactional
  public List<Doctor> getDoctors() {
    List<Doctor> doctors = doctorRepository.findAll();
    doctors.forEach(d -> d.getAvailableTimes().size());
    return doctors;
  }

  public ServiceResult deleteDoctor(long doctorId) {
    try {
      if (!doctorRepository.existsById(doctorId)) {
        return ServiceResult.NOT_FOUND;
      }
      appointmentRepository.deleteAllByDoctorId(doctorId);
      doctorRepository.deleteById(doctorId);
      return ServiceResult.SUCCESS;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return ServiceResult.CONFLICT;
    }
  }

  @Transactional
  public List<Doctor> findDoctorByName(String name) {
    List<Doctor> doctors = doctorRepository.findByNameContaining(name);
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