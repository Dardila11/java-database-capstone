import { apiFetch } from "@/lib/apiClient";

export const adminLogin = (username: string, password: string) =>
  apiFetch<{ token: string }>("/admin/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });

export const patientLogin = (email: string, password: string) =>
  apiFetch<{ token: string }>("/patient/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });

export const doctorLogin = (email: string, password: string) =>
  apiFetch<{ token: string }>("/doctor/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
