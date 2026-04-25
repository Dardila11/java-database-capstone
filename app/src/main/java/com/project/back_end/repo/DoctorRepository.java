package com.project.back_end.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Doctor findByEmail(String email);

    @Query("SELECT d FROM doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(String name);

    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);

    List<Doctor> findBySpecialtyIgnoreCase(String specialty);

    List<Doctor> findByNameContaining(String name);

}