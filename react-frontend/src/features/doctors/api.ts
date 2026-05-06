import { apiFetch } from "@/lib/apiClient";
import type { Doctor, DoctorCreateRequest } from "@/types/api";

const auth = (token: string) => ({ "Authorization": `Bearer ${token}` });

export const getDoctors = () =>
  apiFetch<{ doctors: Doctor[] }>("/doctor");

export const filterDoctors = (name: string, time: string, specialty: string) =>
  apiFetch<{ doctors: Doctor[] }>(`/doctor/filter/${name || "null"}/${time || "null"}/${specialty || "null"}`);

export const saveDoctor = (doctor: DoctorCreateRequest, token: string) =>
  apiFetch<{ success: string }>("/doctor/", {
    method: "POST",
    headers: auth(token),
    body: JSON.stringify(doctor),
  });

export const updateDoctor = (doctor: Doctor, token: string) =>
  apiFetch<{ success: string }>("/doctor/", {
    method: "PUT",
    headers: auth(token),
    body: JSON.stringify(doctor),
  });

export const deleteDoctor = (id: number, token: string) =>
  apiFetch<{ success: string }>(`/doctor/${id}`, {
    method: "DELETE",
    headers: auth(token),
  });

export const getDoctorAvailability = (user: string, doctorId: number, date: string, token: string) =>
  apiFetch<{ availability: string[] }>(`/doctor/availability/${user}/${doctorId}/${date}`, {
    headers: auth(token),
  });
