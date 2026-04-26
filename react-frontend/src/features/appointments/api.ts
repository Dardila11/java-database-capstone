import { apiFetch } from "@/lib/apiClient";
import type { AppointmentDTO, AppointmentCreateRequest, AppointmentUpdateRequest } from "@/types/api";

export const getAppointmentsByDateAndPatient = (date: string, patientName: string, token: string) =>
  apiFetch<{ appointments: AppointmentDTO[] }>(`/appointments/${date}/${patientName || "null"}/${token}`);

export const bookAppointment = (appointment: AppointmentCreateRequest, token: string) =>
  apiFetch<{ message: string }>(`/appointments/${token}`, {
    method: "POST",
    body: JSON.stringify(appointment),
  });

export const updateAppointment = (appointment: AppointmentUpdateRequest, token: string) =>
  apiFetch<{ success: string }>(`/appointments/${token}`, {
    method: "PUT",
    body: JSON.stringify(appointment),
  });

export const cancelAppointment = (id: number, token: string) =>
  apiFetch<{ success: string }>(`/appointments/${id}/${token}`, {
    method: "DELETE",
  });
