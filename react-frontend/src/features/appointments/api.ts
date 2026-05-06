import { apiFetch } from "@/lib/apiClient";
import type { AppointmentDTO, AppointmentCreateRequest, AppointmentUpdateRequest } from "@/types/api";

const auth = (token: string) => ({ "Authorization": `Bearer ${token}` });

export const getAppointmentsByDateAndPatient = (date: string, patientName: string, token: string) =>
  apiFetch<{ appointments: AppointmentDTO[] }>(`/appointments/${date}/${patientName || "null"}/`, {
    headers: auth(token),
  });

export const bookAppointment = (appointment: AppointmentCreateRequest, token: string) =>
  apiFetch<{ message: string }>("/appointments/", {
    method: "POST",
    headers: auth(token),
    body: JSON.stringify(appointment),
  });

export const updateAppointment = (appointment: AppointmentUpdateRequest, token: string) =>
  apiFetch<{ success: string }>("/appointments/", {
    method: "PUT",
    headers: auth(token),
    body: JSON.stringify(appointment),
  });

export const cancelAppointment = (id: number, token: string) =>
  apiFetch<{ success: string }>(`/appointments/${id}`, {
    method: "DELETE",
    headers: auth(token),
  });
