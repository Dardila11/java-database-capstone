import { Avatar } from "@/features/doctors/Avatar";
import { StatusBadge } from "./StatusBadge";
import { formatDate, formatTime, getInitials } from "@/lib/utils";
import type { AppointmentDTO } from "@/types/api";

const COLORS = ["#0d9488","#7c3aed","#0369a1","#b45309","#be185d","#065f46"];

function statusLabel(status: number): string {
  return status === 1 ? "Completed" : status === 2 ? "Inactive" : "Active";
}

interface Props {
  appt: AppointmentDTO;
  perspective?: "doctor" | "patient";
  onPrescription?: (appt: AppointmentDTO) => void;
  onPatientRecord?: (appt: AppointmentDTO) => void;
  onEdit?: (appt: AppointmentDTO) => void;
  onCancel?: (id: number) => void;
}

export function AppointmentRow({ appt, perspective = "doctor", onPrescription, onPatientRecord, onEdit, onCancel }: Props) {
  const color = COLORS[appt.patientId % COLORS.length];
  const initials = getInitials(appt.patientName);

  return (
    <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 0", borderBottom: "1px solid var(--border)" }}>
      <Avatar initials={initials} color={color} size={36} />
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 14, fontWeight: 600 }}>{perspective === "doctor" ? appt.patientName : appt.doctorName}</div>
        <div style={{ fontSize: 12, color: "var(--ink-3)" }}>{perspective === "doctor" ? appt.patientPhone : appt.patientEmail}</div>
      </div>
      <div style={{ textAlign: "right" }}>
        <div style={{ fontSize: 13, fontWeight: 600 }}>{formatDate(appt.appointmentTime)} · {formatTime(appt.appointmentTime)}</div>
        <StatusBadge status={statusLabel(appt.status)} />
      </div>
      <div style={{ display: "flex", gap: 6 }}>
        {perspective === "doctor" && appt.status === 0 && onPrescription && (
          <button onClick={() => onPrescription(appt)} style={{ padding: "6px 10px", borderRadius: 7, border: "1.5px solid var(--teal-light)", background: "var(--teal-light)", cursor: "pointer", color: "var(--teal)", fontSize: 12, fontWeight: 600 }}>📋 Rx</button>
        )}
        {perspective === "doctor" && onPatientRecord && (
          <button onClick={() => onPatientRecord(appt)} style={{ padding: "6px 10px", borderRadius: 7, border: "1.5px solid var(--border)", background: "white", cursor: "pointer", color: "var(--ink-2)", fontSize: 12 }}>📁</button>
        )}
        {perspective === "patient" && appt.status === 0 && onEdit && (
          <button onClick={() => onEdit(appt)} style={{ padding: "6px 10px", borderRadius: 7, border: "1.5px solid var(--border)", background: "white", cursor: "pointer", color: "var(--ink-2)", fontSize: 12 }}>✏️</button>
        )}
        {perspective === "patient" && appt.status === 0 && onCancel && (
          <button onClick={() => onCancel(appt.id)} style={{ padding: "6px 10px", borderRadius: 7, border: "1.5px solid #fee2e2", background: "#fef2f2", cursor: "pointer", color: "#ef4444", fontSize: 12 }}>🗑️</button>
        )}
      </div>
    </div>
  );
}
