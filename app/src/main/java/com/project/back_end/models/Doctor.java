package com.project.back_end.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
(name = "doctors")
public class Doctor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Name is required")
  @Size(min = 3, max = 100)
  private String name;
  @NotNull(message = "Specialty is required")
  @Size(min = 3, max = 50)
  private String specialty;
  @NotNull(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;
  @NotNull(message = "Password is required")
  @Size(min = 6)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;
  @NotNull(message = "Phone is required")
  @Size(min = 10, max = 10)
  @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number format")
  private String phone;
  @ElementCollection
  // Each time slot is represented as a string (e.g., "09:00-10:00", "10:00-11:00").
  // ensures that the list of available times is stored as a separate collection in the database.
  private List<String> availableTimes;

  // constructors
  public Doctor(String name, String specialty, String email, String password, String phone, List<String> availableTimes) {
    this.name = name;
    this.specialty = specialty;
    this.email = email;
    this.password = password;
    this.phone = phone;
    this.availableTimes = availableTimes;
  }
  public Doctor() {
  }

  // getters
  public Long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public String getSpecialty() {
    return specialty;
  }
  public String getEmail() {
    return email;
  }
  public String getPassword() {
    return password;
  }
  public String getPhone() {
    return phone;
  }
  public List<String> getAvailableTimes() {
    return availableTimes;
  }

  //setters
  public void setId(Long id) {
    this.id = id;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setSpecialty(String specialty) {
    this.specialty = specialty;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public void setPassword(String password) {
    this.password = password;
  }
  public void setPhone(String phone) {
    this.phone = phone;
  }
  public void setAvailableTimes(List<String> availableTimes) {
    this.availableTimes = availableTimes;
  }

}

