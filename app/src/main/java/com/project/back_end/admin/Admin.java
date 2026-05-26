package com.project.back_end.admin;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

@Entity(name = "admin")
public class Admin {

  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotNull(message = "Username is required")
  private String username;
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotNull(message = "Password is required")
  private String password;

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  } 

  @Override
  public String toString() {
    return "Admin [id=" + id + ", username=" + username + ", password=" + password + "]";
  }


}
