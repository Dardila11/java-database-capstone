import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getDoctors, filterDoctors, saveDoctor, updateDoctor, deleteDoctor } from "./api";
import type { Doctor } from "@/types/api";

export function useAllDoctors() {
  return useQuery({
    queryKey: ["doctors"],
    queryFn: async () => {
      const res = await getDoctors();
      return res.doctors;
    },
  });
}

export function useFilterDoctors(name: string, time: string, specialty: string, enabled: boolean) {
  return useQuery({
    queryKey: ["doctors", "filter", { name, time, specialty }],
    queryFn: async () => {
      const res = await filterDoctors(name, time, specialty);
      return res.doctors;
    },
    enabled,
  });
}

export function useSaveDoctor(token: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (doctor: Omit<Doctor, "id">) => saveDoctor(doctor, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["doctors"] }),
  });
}

export function useUpdateDoctor(token: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (doctor: Doctor) => updateDoctor(doctor, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["doctors"] }),
  });
}

export function useDeleteDoctor(token: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deleteDoctor(id, token),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["doctors"] }),
  });
}
