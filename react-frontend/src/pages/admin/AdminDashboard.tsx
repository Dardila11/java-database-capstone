import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/AuthContext";
import { DoctorGrid } from "@/features/doctors/DoctorGrid";
import { Avatar } from "@/features/doctors/Avatar";
import { StatusBadge } from "@/features/appointments/StatusBadge";
import { useAllDoctors, useSaveDoctor, useDeleteDoctor } from "@/features/doctors/hooks";
import { useDoctorAppointments } from "@/features/appointments/hooks";
import { AppointmentRow } from "@/features/appointments/AppointmentRow";
import { getInitials, todayISO } from "@/lib/utils";
import type { Doctor } from "@/types/api";

const COLORS = ["#0d9488","#7c3aed","#0369a1","#b45309","#be185d","#065f46"];

function StatCard({ label, value, sub, color = "var(--teal)" }: { label: string; value: string | number; sub?: string; color?: string }) {
  return (
    <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: "20px 22px", boxShadow: "var(--shadow-sm)" }}>
      <div style={{ fontSize: 26, fontWeight: 800, color, letterSpacing: "-0.5px" }}>{value}</div>
      <div style={{ fontSize: 14, fontWeight: 600, color: "var(--ink)", marginTop: 4 }}>{label}</div>
      {sub && <div style={{ fontSize: 12, color: "var(--ink-3)", marginTop: 2 }}>{sub}</div>}
    </div>
  );
}

export function AdminDashboard() {
  const { token, logout } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<"overview" | "doctors" | "appointments">("overview");

  const { data: doctors = [], isLoading: loadingDoctors } = useAllDoctors();
  const today = todayISO();
  const { data: todayAppts = [] } = useDoctorAppointments(today, "null", token);
  const { mutateAsync: saveDoctor } = useSaveDoctor(token!);
  const { mutateAsync: deleteDoctor } = useDeleteDoctor(token!);

  const handleLogout = () => { logout(); navigate("/"); };

  const tabs = [
    { key: "overview" as const, label: "Overview", icon: "📊" },
    { key: "doctors" as const, label: "Doctors", icon: "👥" },
    { key: "appointments" as const, label: "Appointments", icon: "📅" },
  ];

  return (
    <div style={{ maxWidth: 1140, margin: "0 auto", padding: "28px 24px" }}>
      {/* Top bar */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <div>
          <div style={{ fontSize: 12, color: "var(--ink-3)", fontWeight: 500 }}>Good morning,</div>
          <div style={{ fontSize: 24, fontWeight: 800, letterSpacing: "-0.4px" }}>
            Admin <span style={{ fontSize: 14, color: "var(--teal)", background: "var(--teal-light)", borderRadius: 6, padding: "2px 8px" }}>Administrator</span>
          </div>
        </div>
        <button onClick={handleLogout} style={{ display: "flex", alignItems: "center", gap: 6, padding: "9px 16px", borderRadius: 99, border: "1.5px solid var(--border)", background: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", color: "var(--ink-2)" }}>
          🚪 Sign out
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: "flex", gap: 2, borderBottom: "2px solid var(--border)", marginBottom: 28 }}>
        {tabs.map((t) => (
          <button key={t.key} onClick={() => setTab(t.key)} style={{ display: "flex", alignItems: "center", gap: 7, padding: "10px 18px", border: "none", background: "none", cursor: "pointer", fontSize: 14, fontWeight: 600, color: tab === t.key ? "var(--teal)" : "var(--ink-3)", borderBottom: `2px solid ${tab === t.key ? "var(--teal)" : "transparent"}`, marginBottom: -2, transition: "color 0.15s" }}>
            {t.icon} {t.label}
            {t.key === "doctors" && <span style={{ fontSize: 11, fontWeight: 700, padding: "1px 7px", borderRadius: 99, background: tab === "doctors" ? "var(--teal-light)" : "var(--bg)", color: tab === "doctors" ? "var(--teal)" : "var(--ink-3)" }}>{doctors.length}</span>}
          </button>
        ))}
      </div>

      {/* Overview tab */}
      {tab === "overview" && (
        <div style={{ animation: "slideIn 0.18s ease" }}>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(4,1fr)", gap: 14, marginBottom: 28 }}>
            <StatCard label="Total Patients" value="12,420" sub="↑ 3.2% this week" color="var(--teal)" />
            <StatCard label="Appointments Today" value={todayAppts.length} sub={`${todayAppts.filter(a => a.status === 0).length} pending`} color="var(--indigo)" />
            <StatCard label="Active Doctors" value={doctors.filter((d: Doctor) => !d).length || doctors.length} sub="On staff" color="#f59e0b" />
            <StatCard label="Revenue (Apr)" value="$184K" sub="↑ 7.1% vs last month" color="#22c55e" />
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 20 }}>
            <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 22, boxShadow: "var(--shadow-sm)" }}>
              <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 14 }}>Today's Appointments</div>
              {todayAppts.length === 0 ? (
                <div style={{ fontSize: 14, color: "var(--ink-3)", textAlign: "center", padding: "24px 0" }}>No appointments today</div>
              ) : (
                todayAppts.slice(0, 5).map((a) => (
                  <AppointmentRow key={a.id} appt={a} perspective="doctor" />
                ))
              )}
            </div>
            <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 22, boxShadow: "var(--shadow-sm)" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 14 }}>
                <div style={{ fontSize: 16, fontWeight: 700 }}>Doctors Snapshot</div>
                <button onClick={() => setTab("doctors")} style={{ fontSize: 12, color: "var(--teal)", background: "var(--teal-light)", border: "none", borderRadius: 6, padding: "4px 10px", cursor: "pointer", fontWeight: 600 }}>Manage →</button>
              </div>
              {loadingDoctors ? (
                <div style={{ fontSize: 14, color: "var(--ink-3)" }}>Loading…</div>
              ) : (
                doctors.slice(0, 5).map((d: Doctor) => {
                  const color = COLORS[d.id % COLORS.length];
                  return (
                    <div key={d.id} style={{ display: "flex", alignItems: "center", gap: 10, padding: "10px 0", borderBottom: "1px solid var(--border)" }}>
                      <Avatar initials={getInitials(d.name)} color={color} size={34} />
                      <div style={{ flex: 1 }}>
                        <div style={{ fontSize: 13, fontWeight: 600 }}>{d.name}</div>
                        <div style={{ fontSize: 12, color: "var(--ink-3)" }}>{d.specialty}</div>
                      </div>
                      <StatusBadge status="Active" />
                    </div>
                  );
                })
              )}
              {doctors.length > 5 && (
                <button onClick={() => setTab("doctors")} style={{ marginTop: 12, width: "100%", padding: "8px", borderRadius: 8, border: "1.5px dashed var(--border)", background: "var(--bg)", fontSize: 12, color: "var(--ink-3)", cursor: "pointer", fontWeight: 600 }}>
                  +{doctors.length - 5} more doctors →
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Doctors tab */}
      {tab === "doctors" && (
        <DoctorGrid
          doctors={doctors}
          isAdmin
          onDelete={(id) => deleteDoctor(id)}
          onAdd={async (doc) => { await saveDoctor(doc); }}
        />
      )}

      {/* Appointments tab */}
      {tab === "appointments" && (
        <div style={{ background: "white", borderRadius: "var(--radius)", border: "1.5px solid var(--border)", padding: 24, boxShadow: "var(--shadow-sm)", animation: "slideIn 0.18s ease" }}>
          <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 18 }}>All Appointments — {new Date().toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}</div>
          {todayAppts.length === 0 ? (
            <div style={{ fontSize: 14, color: "var(--ink-3)", textAlign: "center", padding: "40px 0" }}>No appointments scheduled for today</div>
          ) : (
            todayAppts.map((a) => <AppointmentRow key={a.id} appt={a} perspective="doctor" />)
          )}
        </div>
      )}
    </div>
  );
}
