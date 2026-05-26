package com.project.back_end.shared.event;

// Kafka message payload — publish after a successful appointment cancellation.
public record AppointmentCancelledEvent(
    long appointmentId,
    long patientId
) {}
