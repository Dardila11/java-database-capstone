import { apiFetch } from "@/lib/apiClient";
import type { Prescription, PrescriptionCreateRequest } from "@/types/api";

export const savePrescription = (prescription: PrescriptionCreateRequest, token: string) =>
  apiFetch<{ success: string }>(`/prescription/${token}`, {
    method: "POST",
    body: JSON.stringify(prescription),
  });

export const getPrescription = (appointmentId: number, token: string) =>
  apiFetch<{ prescription: Prescription[] }>(`/prescription/${appointmentId}/${token}`);
