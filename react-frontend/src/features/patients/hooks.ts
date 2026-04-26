import { useQuery, useMutation } from "@tanstack/react-query";
import { getPatientByToken, getPatientAppointments, filterPatientAppointments, createPatient } from "./api";
import type { PatientCreateRequest } from "@/types/api";

export function usePatientByToken(token: string | null) {
  return useQuery({
    queryKey: ["patient", token],
    queryFn: () => getPatientByToken(token!),
    enabled: !!token,
  });
}

export function usePatientAppointments(patientId: number | null, token: string | null) {
  return useQuery({
    queryKey: ["patientAppointments", patientId],
    queryFn: async () => {
      const res = await getPatientAppointments(patientId!, token!);
      return res.appointments;
    },
    enabled: !!patientId && !!token,
  });
}

export function useFilterPatientAppointments(condition: string, name: string, token: string | null, enabled: boolean) {
  return useQuery({
    queryKey: ["patientAppointments", "filter", { condition, name }],
    queryFn: async () => {
      const res = await filterPatientAppointments(condition, name, token!);
      return res.appointments;
    },
    enabled: !!token && enabled,
  });
}

export function useCreatePatient() {
  return useMutation({
    mutationFn: (data: PatientCreateRequest) => createPatient(data),
  });
}
