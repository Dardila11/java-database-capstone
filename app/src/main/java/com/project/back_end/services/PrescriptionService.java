package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository){
        this.prescriptionRepository = prescriptionRepository;
    }

    public int savePrescription(Prescription prescription) {
        try {
            List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
            if (!existing.isEmpty()) {
                return -1;
            }
            prescriptionRepository.save(prescription);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Prescription> getPrescription(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }


}
