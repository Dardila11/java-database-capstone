import { apiFetch } from "@/lib/apiClient";
import type { Patient, AppointmentDTO, PatientCreateRequest } from "@/types/api";

const auth = (token: string) => ({ "Authorization": `Bearer ${token}` });

export const createPatient = (data: PatientCreateRequest) =>
  apiFetch<{ message: string }>("/patient/create", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getPatientByToken = (token: string) =>
  apiFetch<{ patient: Patient }>("/patient/", {
    headers: auth(token),
  });

export const getPatientAppointments = (patientId: number, token: string) =>
  apiFetch<{ appointments: AppointmentDTO[] }>(`/patient/${patientId}`, {
    headers: auth(token),
  });

export const filterPatientAppointments = (condition: string, name: string, token: string) =>
  apiFetch<{ appointments: AppointmentDTO[] }>(`/patient/filter/${condition || "null"}/${name || "null"}`, {
    headers: auth(token),
  });
