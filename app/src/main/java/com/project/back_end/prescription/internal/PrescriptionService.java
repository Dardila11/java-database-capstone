package com.project.back_end.prescription.internal;

import com.project.back_end.enums.ServiceResult;
import com.project.back_end.prescription.Prescription;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public ServiceResult savePrescription(Prescription prescription) {
        try {
            List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
            if (!existing.isEmpty()) {
                return ServiceResult.DUPLICATE;
            }
            prescriptionRepository.save(prescription);
            return ServiceResult.SUCCESS;
        } catch (Exception e) {
            return ServiceResult.CONFLICT;
        }
    }

    public List<Prescription> getPrescription(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }
}
