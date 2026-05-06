export type UserRole = "admin" | "doctor" | "patient" | "loggedPatient";

export interface Doctor {
  id: number;
  name: string;
  specialty: string;
  email: string;
  phone: string;
  availableTimes: string[];
}

export interface Patient {
  id: number;
  name: string;
  email: string;
  phone: string;
  address: string;
}

export interface AppointmentDTO {
  id: number;
  doctorId: number;
  doctorName: string;
  patientId: number;
  patientName: string;
  patientEmail: string;
  patientPhone: string;
  patientAddress: string;
  appointmentTime: string | number[];
  status: 0 | 1 | 2;
}

export interface Prescription {
  id: string;
  patientName: string;
  appointmentId: number;
  medication: string;
  dosage: string;
  doctorNotes: string;
}

export interface DoctorCreateRequest {
  name: string;
  specialty: string;
  email: string;
  phone: string;
  password: string;
  availableTimes: string[];
}

export interface PatientCreateRequest {
  name: string;
  email: string;
  password: string;
  phone: string;
  address: string;
}

export interface AppointmentCreateRequest {
  doctor: { id: number };
  patient: { id: number };
  appointmentTime: string;
  status: 0;
}

export interface AppointmentUpdateRequest {
  id: number;
  doctor: { id: number };
  patient: { id: number };
  appointmentTime: string;
  status: number;
}

export interface PrescriptionCreateRequest {
  patientName: string;
  appointmentId: number;
  medication: string;
  dosage: string;
  doctorNotes: string;
}
