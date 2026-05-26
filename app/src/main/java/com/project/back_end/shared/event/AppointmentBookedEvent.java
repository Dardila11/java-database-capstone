package com.project.back_end.shared.event;

import java.time.LocalDateTime;

// Kafka message payload — publish after a successful appointment booking.
public record AppointmentBookedEvent(
    long appointmentId,
    long patientId,
    long doctorId,
    LocalDateTime appointmentTime
) {}
