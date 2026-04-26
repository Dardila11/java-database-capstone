import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/AuthContext";
import { usePatientAppointments } from "@/features/patients/hooks";
import { formatDate } from "@/lib/utils";
import type { AppointmentDTO } from "@/types/api";

export function PatientRecordPage() {
  const { patientId } = useParams<{ patientId: string }>();
  const { token } = useAuth();
  const navigate = useNavigate();
  const pid = patientId ? parseInt(patientId) : null;

  const { data: appointments = [], isLoading } = usePatientAppointments(pid, token);

  return (
    <div style={{ maxWidth: 900, margin: "40px auto", padding: "0 24px" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 24 }}>
        <button onClick={() => navigate("/doctor")} style={{ background: "none", border: "none", cursor: "pointer", fontSize: 20, color: "var(--ink-3)" }}>←</button>
        <div>
          <div style={{ fontSize: 20, fontWeight: 800 }}>Patient Record</div>
          <div style={{ fontSize: 13, color: "var(--ink-3)" }}>Patient ID #{patientId}</div>
        </div>
      </div>

      <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", overflow: "hidden", boxShadow: "var(--shadow-sm)" }}>
        <div style={{ padding: "14px 20px", background: "var(--bg)", borderBottom: "1px solid var(--border)", display: "flex", gap: 14 }}>
          {["Date", "Appointment ID", "Doctor", "Status", "Prescription"].map((h) => (
            <div key={h} style={{ flex: 1, fontSize: 11, fontWeight: 700, color: "var(--ink-3)", textTransform: "uppercase", letterSpacing: "0.5px" }}>{h}</div>
          ))}
        </div>

        {isLoading ? (
          <div style={{ padding: "40px", textAlign: "center", color: "var(--ink-3)" }}>Loading…</div>
        ) : appointments.length === 0 ? (
          <div style={{ padding: "40px", textAlign: "center", color: "var(--ink-3)", fontSize: 14 }}>No appointment history found.</div>
        ) : (
          appointments.map((a: AppointmentDTO) => (
            <div key={a.id} style={{ display: "flex", gap: 14, padding: "14px 20px", borderBottom: "1px solid var(--border)", alignItems: "center" }}>
              <div style={{ flex: 1, fontSize: 13 }}>{formatDate(a.appointmentTime)}</div>
              <div style={{ flex: 1, fontSize: 13, color: "var(--ink-3)" }}>#{a.id}</div>
              <div style={{ flex: 1, fontSize: 13, fontWeight: 600 }}>{a.doctorName}</div>
              <div style={{ flex: 1, fontSize: 12, fontWeight: 700, color: a.status === 1 ? "#16a34a" : "#2563eb", background: a.status === 1 ? "#dcfce7" : "#dbeafe", padding: "3px 9px", borderRadius: 99, display: "inline-block" }}>
                {a.status === 1 ? "Completed" : "Scheduled"}
              </div>
              <div style={{ flex: 1 }}>
                <button
                  onClick={() => navigate(`/prescription/${a.id}?patientName=${encodeURIComponent(a.patientName)}&mode=view`)}
                  style={{ padding: "6px 12px", borderRadius: 7, border: "1.5px solid var(--teal-light)", background: "var(--teal-light)", cursor: "pointer", color: "var(--teal)", fontSize: 12, fontWeight: 600 }}
                >
                  📋 View Rx
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
