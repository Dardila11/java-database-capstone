import { apiFetch } from "@/lib/apiClient";
import type { Doctor } from "@/types/api";

export const getDoctors = () =>
  apiFetch<{ doctors: Doctor[] }>("/doctor");

export const filterDoctors = (name: string, time: string, specialty: string) =>
  apiFetch<{ doctors: Doctor[] }>(`/doctor/filter/${name || "null"}/${time || "null"}/${specialty || "null"}`);

export const saveDoctor = (doctor: Omit<Doctor, "id">, token: string) =>
  apiFetch<{ success: string }>(`/doctor/${token}`, {
    method: "POST",
    body: JSON.stringify(doctor),
  });

export const updateDoctor = (doctor: Doctor, token: string) =>
  apiFetch<{ success: string }>(`/doctor/${token}`, {
    method: "PUT",
    body: JSON.stringify(doctor),
  });

export const deleteDoctor = (id: number, token: string) =>
  apiFetch<{ success: string }>(`/doctor/${id}/${token}`, {
    method: "DELETE",
  });

export const getDoctorAvailability = (user: string, doctorId: number, date: string, token: string) =>
  apiFetch<{ availability: string[] }>(`/doctor/availability/${user}/${doctorId}/${date}/${token}`);
