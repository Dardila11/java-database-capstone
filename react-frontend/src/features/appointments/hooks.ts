import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getAppointmentsByDateAndPatient, bookAppointment, updateAppointment, cancelAppointment } from "./api";
import type { AppointmentCreateRequest, AppointmentUpdateRequest } from "@/types/api";

export function useDoctorAppointments(date: string, patientName: string, token: string | null) {
  return useQuery({
    queryKey: ["appointments", date, patientName],
    queryFn: async () => {
      const res = await getAppointmentsByDateAndPatient(date, patientName, token!);
      return res.appointments;
    },
    enabled: !!token && !!date,
  });
}

export function useBookAppointment(token: string | null) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (appointment: AppointmentCreateRequest) => bookAppointment(appointment, token!),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["appointments"] });
      qc.invalidateQueries({ queryKey: ["patientAppointments"] });
    },
  });
}

export function useUpdateAppointment(token: string | null) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (appointment: AppointmentUpdateRequest) => updateAppointment(appointment, token!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["patientAppointments"] }),
  });
}

export function useCancelAppointment(token: string | null) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => cancelAppointment(id, token!),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["patientAppointments"] }),
  });
}
