import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "@/features/auth/AuthContext";
import { useUpdateAppointment } from "@/features/appointments/hooks";
import { useAllDoctors } from "@/features/doctors/hooks";
import { todayISO } from "@/lib/utils";
import type { AppointmentDTO, Doctor } from "@/types/api";

export function UpdateAppointmentPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { token, patientId } = useAuth();
  const appt = location.state?.appt as AppointmentDTO | undefined;

  const { data: doctors = [] } = useAllDoctors();
  const { mutateAsync: update } = useUpdateAppointment(token);

  const [date, setDate] = useState(todayISO());
  const [slot, setSlot] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const doctor = doctors.find((d: Doctor) => d.id === appt?.doctorId);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!appt || !patientId || !slot) return;
    try {
      const [startTime] = slot.split("-");
      const appointmentTime = `${date}T${startTime}:00`;
      await update({
        id: appt.id,
        doctor: { id: appt.doctorId },
        patient: { id: patientId },
        appointmentTime,
        status: appt.status,
      });
      setSuccess(true);
      setTimeout(() => navigate("/patient/appointments"), 1500);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Update failed");
    }
  };

  if (success) {
    return (
      <div style={{ maxWidth: 500, margin: "80px auto", textAlign: "center", padding: "48px 32px", background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)" }}>
        <div style={{ fontSize: 56, marginBottom: 16 }}>✅</div>
        <div style={{ fontSize: 20, fontWeight: 800 }}>Appointment Updated!</div>
      </div>
    );
  }

  const inputStyle: React.CSSProperties = {
    padding: "10px 13px", borderRadius: 10, border: "1.5px solid var(--border)",
    fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%",
  };
  const readOnly: React.CSSProperties = { ...inputStyle, background: "#f8fafc", color: "var(--ink-3)" };

  return (
    <div style={{ maxWidth: 500, margin: "40px auto", padding: "0 24px" }}>
      <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 28, boxShadow: "var(--shadow-sm)" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 24 }}>
          <button onClick={() => navigate("/patient/appointments")} style={{ background: "none", border: "none", cursor: "pointer", fontSize: 20, color: "var(--ink-3)" }}>←</button>
          <div style={{ fontSize: 18, fontWeight: 800 }}>Reschedule Appointment</div>
        </div>

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Patient</label>
            <input value={appt?.patientName ?? ""} readOnly style={readOnly} />
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Doctor</label>
            <input value={appt?.doctorName ?? ""} readOnly style={readOnly} />
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>New Date</label>
            <input type="date" value={date} min={todayISO()} onChange={(e) => setDate(e.target.value)} style={inputStyle} />
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <label style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>Time Slot</label>
            <select value={slot} onChange={(e) => setSlot(e.target.value)} required style={inputStyle}>
              <option value="">Select a time slot</option>
              {(doctor?.availableTimes ?? []).map((t: string) => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>

          {error && <div style={{ fontSize: 13, color: "#ef4444", background: "#fef2f2", borderRadius: 8, padding: "8px 12px" }}>{error}</div>}

          <div style={{ display: "flex", gap: 10 }}>
            <button type="button" onClick={() => navigate("/patient/appointments")} style={{ flex: 1, padding: "12px", borderRadius: 10, border: "1.5px solid var(--border)", background: "white", fontSize: 14, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>Cancel</button>
            <button type="submit" style={{ flex: 2, padding: "12px", borderRadius: 10, border: "none", background: "var(--teal)", color: "white", fontSize: 14, fontWeight: 700, cursor: "pointer" }}>Save Changes</button>
          </div>
        </form>
      </div>
    </div>
  );
}
