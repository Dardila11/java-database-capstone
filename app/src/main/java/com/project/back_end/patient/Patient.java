package com.project.back_end.patient;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity(name = "patient")
public class Patient {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @NotNull(message = "Name is required")
  @Size(min = 3, max = 100)
  private String name;
  
  @NotNull(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;
  
  @NotNull(message = "Password is required")
  @Size(min = 6)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;
  
  @NotNull(message = "Phone is required")
  @Size(min = 10, max = 12)
  @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number format")
  private String phone;
  
  @NotNull(message = "Address is required")
  @Size(max = 255)
  private String address;

  // getters
  public Long getId() {
    return id;
  }
  public String getName() {
    return name;
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
  public String getAddress() {
    return address;
  }

  //setters
  public void setId(Long id) {
    this.id = id;
  }
  public void setName(String name) {
    this.name = name;
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
  public void setAddress(String address) {
    this.address = address;
  }
  

}
