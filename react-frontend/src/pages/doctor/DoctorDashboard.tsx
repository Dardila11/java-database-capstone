import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/AuthContext";
import { AppointmentRow } from "@/features/appointments/AppointmentRow";
import { Avatar } from "@/features/doctors/Avatar";
import { useDoctorAppointments } from "@/features/appointments/hooks";
import { todayISO } from "@/lib/utils";
import type { AppointmentDTO } from "@/types/api";

function StatCard({ label, value, sub, color = "var(--teal)" }: { label: string; value: string | number; sub?: string; color?: string }) {
  return (
    <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: "20px 22px", boxShadow: "var(--shadow-sm)" }}>
      <div style={{ fontSize: 26, fontWeight: 800, color, letterSpacing: "-0.5px" }}>{value}</div>
      <div style={{ fontSize: 14, fontWeight: 600, color: "var(--ink)", marginTop: 4 }}>{label}</div>
      {sub && <div style={{ fontSize: 12, color: "var(--ink-3)", marginTop: 2 }}>{sub}</div>}
    </div>
  );
}

export function DoctorDashboard() {
  const { token, logout } = useAuth();
  const navigate = useNavigate();
  const [date, setDate] = useState(todayISO());
  const [patientSearch, setPatientSearch] = useState("");

  const { data: appointments = [], isLoading } = useDoctorAppointments(date, patientSearch, token);

  const handleLogout = () => { logout(); navigate("/"); };

  return (
    <div style={{ maxWidth: 900, margin: "0 auto", padding: "32px 24px" }}>
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 32 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <Avatar initials="DR" color="#7c3aed" size={52} />
          <div>
            <div style={{ fontSize: 12, color: "var(--ink-3)" }}>Physician Dashboard</div>
            <div style={{ fontSize: 24, fontWeight: 800, letterSpacing: "-0.3px" }}>
              Doctor <span style={{ fontSize: 14, color: "#7c3aed", background: "#ede9fe", borderRadius: 6, padding: "2px 8px" }}>Doctor</span>
            </div>
          </div>
        </div>
        <button onClick={handleLogout} style={{ display: "flex", alignItems: "center", gap: 6, padding: "9px 16px", borderRadius: 99, border: "1.5px solid var(--border)", background: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>
          🚪 Sign out
        </button>
      </div>

      {/* Stats */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 14, marginBottom: 24 }}>
        <StatCard label="Today's Appointments" value={appointments.filter((a: AppointmentDTO) => a.status === 0).length} sub="Scheduled" color="var(--teal)" />
        <StatCard label="Completed Today" value={appointments.filter((a: AppointmentDTO) => a.status === 1).length} sub="With prescriptions" color="var(--indigo)" />
        <StatCard label="Total Appointments" value={appointments.length} sub="This view" color="#f59e0b" />
      </div>

      {/* Schedule */}
      <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 24, boxShadow: "var(--shadow-sm)" }}>
        <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 20, flexWrap: "wrap" }}>
          <div style={{ fontSize: 16, fontWeight: 700, flex: 1 }}>
            Schedule — {new Date(date).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}
          </div>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            style={{ padding: "8px 12px", borderRadius: 10, border: "1.5px solid var(--border)", fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)" }}
          />
          <input
            value={patientSearch}
            onChange={(e) => setPatientSearch(e.target.value)}
            placeholder="Filter by patient name…"
            style={{ padding: "8px 12px", borderRadius: 10, border: "1.5px solid var(--border)", fontSize: 14, outline: "none", fontFamily: "inherit", background: "var(--bg)", color: "var(--ink)", flex: "1 1 180px" }}
          />
          <button onClick={() => setDate(todayISO())} style={{ padding: "8px 14px", borderRadius: 99, border: "none", background: "var(--teal-light)", color: "var(--teal)", fontSize: 13, fontWeight: 600, cursor: "pointer" }}>Today</button>
        </div>

        {isLoading ? (
          <div style={{ fontSize: 14, color: "var(--ink-3)", textAlign: "center", padding: "40px 0" }}>Loading appointments…</div>
        ) : appointments.length === 0 ? (
          <div style={{ fontSize: 14, color: "var(--ink-3)", textAlign: "center", padding: "40px 0" }}>No appointments for this date.</div>
        ) : (
          appointments.map((a: AppointmentDTO) => (
            <AppointmentRow
              key={a.id}
              appt={a}
              perspective="doctor"
              onPrescription={(appt) => navigate(`/prescription/${appt.id}?patientName=${encodeURIComponent(appt.patientName)}&mode=add`)}
              onPatientRecord={(appt) => navigate(`/doctor/patient/${appt.patientId}`)}
            />
          ))
        )}
      </div>
    </div>
  );
}
