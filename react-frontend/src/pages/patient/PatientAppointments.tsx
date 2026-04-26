import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/AuthContext";
import { AppointmentRow } from "@/features/appointments/AppointmentRow";
import { usePatientByToken, usePatientAppointments } from "@/features/patients/hooks";
import { useCancelAppointment } from "@/features/appointments/hooks";
import type { AppointmentDTO } from "@/types/api";

export function PatientAppointments() {
  const { token, patientId, setPatientId, logout } = useAuth();
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<"all" | "upcoming" | "past">("all");

  const { data: patientData } = usePatientByToken(token);
  const { data: appointments = [], isLoading } = usePatientAppointments(patientId, token);
  const { mutateAsync: cancel } = useCancelAppointment(token);

  useEffect(() => {
    if (patientData?.patient && !patientId) {
      setPatientId(patientData.patient.id);
    }
  }, [patientData, patientId, setPatientId]);

  const filtered = appointments.filter((a: AppointmentDTO) => {
    const matchSearch = a.doctorName.toLowerCase().includes(search.toLowerCase());
    const matchFilter =
      filter === "all" ||
      (filter === "upcoming" && a.status === 0) ||
      (filter === "past" && a.status === 1);
    return matchSearch && matchFilter;
  });

  return (
    <div style={{ maxWidth: 900, margin: "0 auto", padding: "32px 24px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 28 }}>
        <div>
          <div style={{ fontSize: 20, fontWeight: 800 }}>My Appointments</div>
          <div style={{ fontSize: 13, color: "var(--ink-3)", marginTop: 4 }}>{appointments.length} total appointments</div>
        </div>
        <div style={{ display: "flex", gap: 10 }}>
          <button onClick={() => navigate("/patient/home")} style={{ padding: "8px 16px", borderRadius: 99, border: "1.5px solid var(--border)", background: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>← Dashboard</button>
          <button onClick={() => { logout(); navigate("/"); }} style={{ padding: "8px 16px", borderRadius: 99, border: "1.5px solid var(--border)", background: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>Sign out</button>
        </div>
      </div>

      {/* Filters */}
      <div style={{ display: "flex", gap: 12, marginBottom: 20, flexWrap: "wrap" }}>
        <div style={{ position: "relative", flex: "1 1 200px" }}>
          <span style={{ position: "absolute", left: 11, top: "50%", transform: "translateY(-50%)", fontSize: 14, color: "var(--ink-3)" }}>🔍</span>
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Filter by doctor name…" style={{ padding: "9px 9px 9px 34px", borderRadius: 10, border: "1.5px solid var(--border)", fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", width: "100%" }} />
        </div>
        <div style={{ display: "flex", gap: 4, background: "var(--bg)", borderRadius: 99, padding: 4, border: "1px solid var(--border)" }}>
          {(["all", "upcoming", "past"] as const).map((f) => (
            <button key={f} onClick={() => setFilter(f)} style={{ padding: "5px 14px", borderRadius: 99, border: "none", cursor: "pointer", fontSize: 12, fontWeight: 600, background: filter === f ? "white" : "transparent", color: filter === f ? "var(--ink)" : "var(--ink-3)", boxShadow: filter === f ? "var(--shadow-sm)" : "none", transition: "all 0.15s", textTransform: "capitalize" }}>{f}</button>
          ))}
        </div>
      </div>

      <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: "0 20px", boxShadow: "var(--shadow-sm)" }}>
        {isLoading ? (
          <div style={{ padding: "40px", textAlign: "center", color: "var(--ink-3)", fontSize: 14 }}>Loading appointments…</div>
        ) : filtered.length === 0 ? (
          <div style={{ padding: "40px", textAlign: "center", color: "var(--ink-3)", fontSize: 14 }}>No appointments found.</div>
        ) : (
          filtered.map((a: AppointmentDTO) => (
            <AppointmentRow
              key={a.id}
              appt={a}
              perspective="patient"
              onEdit={(appt) => navigate(`/patient/appointments/${appt.id}/edit`, { state: { appt } })}
              onCancel={async (id) => { await cancel(id); }}
            />
          ))
        )}
      </div>
    </div>
  );
}
