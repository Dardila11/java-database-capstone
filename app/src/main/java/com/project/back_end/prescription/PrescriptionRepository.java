package com.project.back_end.prescription;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String>  {
      List<Prescription> findByAppointmentId(Long appointmentId);
}

