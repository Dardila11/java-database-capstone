import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getPrescription, savePrescription } from "./api";
import type { PrescriptionCreateRequest } from "@/types/api";

export function usePrescription(appointmentId: number | null, token: string | null) {
  return useQuery({
    queryKey: ["prescription", appointmentId],
    queryFn: async () => {
      const res = await getPrescription(appointmentId!, token!);
      return res.prescription;
    },
    enabled: !!appointmentId && !!token,
  });
}

export function useSavePrescription(token: string | null) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: PrescriptionCreateRequest) => savePrescription(data, token!),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ["prescription", vars.appointmentId] }),
  });
}
